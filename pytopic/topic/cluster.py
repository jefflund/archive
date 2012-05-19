"""Implementation of ClusterLDA, first described by Lund in 2011"""

import math
from pytopic.topic.model import TopicModel, top_n
from pytopic.util.sample import sample_uniform, sample_order, sample_lcounts

class ClusterLDA(TopicModel):
    """ClusterLDA, which combines LDA with Mixture of Multinomials"""

    # forgive the horrible variable names - they match my white board...

    # z_dn = topic of the nth word of document d
    # k_d = cluster of the document d

    # l_k = number of documents assigned to cluster k
    # s_t = number of tokens with topic t
    # h_kt = number of tokens with topic t in documents assigned to cluster k
    # p_tv = number of tokens of type v assigned to topic t
    # r_dt = number of tokens in document d with topic t
    # p_k = number of tokens in documents assigned to cluster k

    def __init__(self, corpus, K, T, gamma, alpha, beta):
        TopicModel.__init__(self, corpus)

        self.K = K
        self.T = T
        self.gamma = gamma
        self.alpha = alpha
        self.beta = beta

        self.Talpha = self.T * self.alpha
        self.Vbeta = self.V * self.beta

        self.k = [0 for _ in range(self.M)]
        self.z = [[0 for _ in range(size)] for size in self.N]

        self.l = [0 for _ in range(self.K)]
        self.s = [0 for _ in range(self.T)]
        self.h = [[0 for _ in range(self.T)] for _ in range(self.K)]
        self.p = [[0 for _ in range(self.V)] for _ in range(self.T)]
        self.r = [[0 for _ in range(self.T)] for _ in range(self.M)]
        self.q = [0 for _ in range(self.K)]

        for d in range(self.M):
            self.k[d] = sample_uniform(self.K)
            self.l[self.k[d]] += 1
            self.q[self.k[d]] += self.N[d]

            for n in range(self.N[d]):
                self.set_z(d, n, sample_uniform(self.T))

    def set_anneal_temp(self, t):
        self.sample_lcounts = lambda xs: sample_lcounts([x * t for x in xs])

    def sample(self):
        for d in sample_order(self.M):
            self.sample_k(d)
            for n in sample_order(self.N[d]):
                self.sample_z(d, n)

    def sample_z(self, d, n):
        """
        ClusterLDA.sample_z(int, int): return None
        Samples the value of z_dn according to the complete conditional
        """

        self.unset_z(d, n)
        counts = [self.prob_z(d, n, j) for j in range(self.T)]
        self.set_z(d, n, self.sample_counts(counts))

    def prob_z(self, d, n, j):
        """
        ClusterLDA.prob_z(int, int, int): return float
        Returns the probability p(z_dn=j|w, z_-dn, alpha, beta)
        """

        prob = self.alpha + self.h[self.k[d]][j]
        prob *= self.beta + self.p[j][self.w[d][n]]
        prob /= self.Vbeta + self.s[j]
        return prob

    def set_z(self, d, n, z_dn):
        """
        ClusterLDA.set_z(int, int, int): return None
        Sets the value of z_dn and updates the counters. Does not adjust the
        counters for the previous value of z_dn.
        """

        self.z[d][n] = z_dn

        self.s[z_dn] += 1
        self.h[self.k[d]][z_dn] += 1
        self.p[z_dn][self.w[d][n]] += 1
        self.r[d][z_dn] += 1

    def unset_z(self, d, n):
        """
        ClusterLDA.unset_z(int, int): return None
        Adjust the counters so that z_dn is not used in the counts.
        """

        self.s[self.z[d][n]] -= 1
        self.h[self.k[d]][self.z[d][n]] -= 1
        self.p[self.z[d][n]][self.w[d][n]] -= 1
        self.r[d][self.z[d][n]] -= 1

    def sample_k(self, d):
        """
        ClusterLDA.sample_k(int): return None
        Samples the value of k_d according to the complete conditional
        """

        lcounts = []
        for j in range(self.K):
            self.set_k(d, j)
            lcounts.append(self.lprob_k(d, j))
        self.set_k(d, self.sample_lcounts(lcounts))

    def lprob_k(self, d, j):
        """
        ClusterLDA.lprob_k(int, int): return float
        Returns the log probability log p(k_d=j| w, z, k_-d, alpha, beta)
        """

        prob = math.log(self.gamma + self.l[j] - 1)
        for t in set(self.z[d]):
            prob += math.lgamma(self.alpha + self.h[j][t])
            prob -= math.lgamma(self.alpha + self.h[j][t] - self.r[d][t])
        prob += math.lgamma(self.Talpha + self.q[j] - self.N[d])
        prob -= math.lgamma(self.Talpha + self.q[j])
        return prob

    def set_k(self, d, k_d):
        """
        ClusterLDA.set_k(int, int): return None
        Updates the value k_d along with all counters. This method also adjust
        the counters related to the previous value of k_d
        """

        self.l[self.k[d]] -= 1
        for z in self.z[d]:
            self.h[self.k[d]][z] -= 1
        self.q[self.k[d]] -= self.N[d]

        self.k[d] = k_d

        self.l[self.k[d]] += 1
        for z in self.z[d]:
            self.h[self.k[d]][z] += 1
        self.q[self.k[d]] += self.N[d]

    def topic_words(self, t, n):
        """
        ClusterLDA.topic_words(int, int): return list of int
        Returns the top n words for topic t
        """

        return top_n(self.p[t], n)

    def cluster_topics(self, k, n):
        """
        ClusterLDA.cluster_topics(int, int): return list of int
        Returns the top n topics for the cluster k
        """

        return top_n(self.h[k], n)

    def doc_topics(self, d, n):
        """
        ClusterLDA.doc_topics(int, int): return int
        Returns the top n topics for the document d
        """

        return top_n(self.r[d], n)

    def print_state(self, verbose=False):
        for k in range(self.K):
            print '{} -'.format(k),
            for t in self.cluster_topics(k, 5):
                print '{}('.format(t),
                for v in self.topic_words(t, 3):
                    print self.vocab[v],
                print ')',
            print

        if verbose:
            for t in range(self.T):
                print '{} -'.format(t),
                for v in self.topic_words(t, 15):
                    print self.vocab[v],
                print

            for d in range(self.M):
                print '{} ({}) -'.format(self.titles[d], self.k[d]),
                for t in self.doc_topics(d, 5):
                    print '{}('.format(t),
                    for v in self.topic_words(t, 2):
                        print self.vocab[v],
                    print ')',
                print
