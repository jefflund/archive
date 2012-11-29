"""Implementation of LDA as described by Blei in 2003"""

from pytopic.topic.model import TopicModel
from pytopic.util.compute import sample_uniform, sample_counts, top_n

class VanillaLDA(TopicModel):
    """Latent Dirichlet Allocation with a Gibbs sampler"""

    def __init__(self, corpus, T, alpha, beta):
        TopicModel.__init__(self, corpus)

        self.T = T
        self.alpha = alpha
        self.beta = beta
        self.Vbeta = self.V * self.beta

        self.z = [[0 for _ in range(size)] for size in self.N]

        self.c_t = [0 for _ in range(self.T)]
        self.c_dt = [[0 for _ in range(self.T)] for _ in range(self.M)]
        self.c_tv = [[0 for _ in range(self.V)] for _ in range(self.T)]

        for d in range(self.M):
            for n in range(self.N[d]):
                self.set_z(d, n, sample_uniform(self.T))

    def sample(self):
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

        prob = self.alpha + self.c_dt[d][j]
        prob *= self.beta + self.c_tv[j][self.w[d][n]]
        prob /= self.Vbeta + self.c_t[j]
        return prob

    def set_z(self, d, n, z_dn):
        """
        VanillaLDA.set_z(int, int, int): return None
        Sets the value of z_dn and updates the counters. Does not adjust the
        counters for the previous value of z_dn.
        """

        self.z[d][n] = z_dn

        self.c_t[z_dn] += 1
        self.c_dt[d][z_dn] += 1
        self.c_tv[z_dn][self.w[d][n]] += 1

    def unset_z(self, d, n):
        """
        VanillaLDA.unset_z(int, int): return None
        Adjust the counters so that z_dn is not used in the counts.
        """

        z_dn = self.z[d][n]

        self.c_t[z_dn] -= 1
        self.c_dt[d][z_dn] -= 1
        self.c_tv[z_dn][self.w[d][n]] -= 1

    def topic_words(self, t, n):
        """
        VanillaLDA.topic_words(int, int): return list of int
        Returns the top n words in topic t
        """

        return top_n(self.c_tv[t], n)

    def doc_topics(self, d, n):
        """
        VanillaLDA.doc_topics(int, int): return list of int
        Returns the top n topics in document d
        """

        return top_n(self.c_dt[d], n)

    def print_state(self, verbose=False):
        for t in range(self.T):
            print '{0} -'.format(t),
            for v in self.topic_words(t, 15):
                print self.vocab[v],
            print

        if verbose:
            for d in range(self.M):
                print '{0} -'.format(self.titles[d]),
                for t in self.doc_topics(d, 5):
                    print '{0}('.format(t),
                    for v in self.topic_words(t, 2):
                        print self.vocab[v],
                    print ')',
                print

        print
