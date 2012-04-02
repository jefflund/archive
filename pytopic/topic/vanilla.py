"""Implementation of LDA as described by Blei in 2003"""

from topic.model import TopicModel, top_n
from util.sample import sample_uniform, sample_counts

class VanillaLDA(TopicModel):
    """Latent Dirichlet Allocation with a Gibbs sampler"""

    def __init__(self, corpus, T, alpha, beta):
        TopicModel.__init__(self, corpus)

        self.T = T
        self.alpha = alpha
        self.beta = beta
        self.Vbeta = self.V * self.beta

        self.z = [[0 for _ in range(size)] for size in self.N]

        self.s = [0 for _ in range(self.T)]
        self.h = [[0 for _ in range(self.T)] for _ in range(self.M)]
        self.p = [[0 for _ in range(self.V)] for _ in range(self.T)]

        for d in range(self.M):
            for n in range(self.N[d]):
                self.set_z(d, n, sample_uniform(self.T))

    def iteration(self):
        for d in range(self.M):
            for n in range(self.N[d]):
                self.sample_z(d, n)

    def sample_z(self, d, n):
        """
        VanillaLDA.sample_z(int, int): return None
        Samples the value of z_dn according to the complete conditional
        """

        self.unset_z(d, n)
        counts = [self.prob_z(d, n, j) for j in range(self.T)]
        self.set_z(d, n, sample_counts(counts))

    def prob_z(self, d, n, j):
        """
        VanillaLDA.prob_z(int, int, int): return float
        Returns the probability p(z_dn=j|w, z_-dn, alpha, beta)
        """

        prob = self.alpha + self.h[d][j]
        prob *= self.beta + self.p[j][self.w[d][n]]
        prob /= self.Vbeta + self.s[j]
        return prob

    def set_z(self, d, n, z_dn):
        """
        VanillaLDA.set_z(int, int, int): return None
        Sets the value of z_dn and updates the counters. Does not adjust the
        counters for the previous value of z_dn.
        """

        self.z[d][n] = z_dn

        self.s[z_dn] += 1
        self.h[d][z_dn] += 1
        self.p[z_dn][self.w[d][n]] += 1

    def unset_z(self, d, n):
        """
        VanillaLDA.unset_z(int, int): return None
        Adjust the counters so that z_dn is not used in the counts.
        """

        self.s[self.z[d][n]] -= 1
        self.h[d][self.z[d][n]] -= 1
        self.p[self.z[d][n]][self.w[d][n]] -= 1

    def topic_words(self, t, n):
        """
        VanillaLDA.topic_words(int, int): return list of int
        Returns the top n words in topic t
        """

        return top_n(self.p[t], n)

    def doc_topics(self, d, n):
        """
        VanillaLDA.doc_topics(int, int): return list of int
        Returns the top n topics in document d
        """

        return top_n(self.h[d], n)
