"""Implementation of ClusterLDA, first described by Lund in 2011"""

import math
from pytopic.topic.model import TopicModel
from pytopic.util.compute import (sample_uniform, sample_order, sample_lcounts,
                                  sample_counts, top_n)
from pytopic.util.data import init_counter

class ClusterLDA(TopicModel):
    """ClusterLDA, which combines LDA with Mixture of Multinomials"""

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

        self.c_k_doc = init_counter(self.K)
        self.c_t = init_counter(self.T)
        self.c_kt = init_counter(self.K, self.T)
        self.c_tv = init_counter(self.T, self.V)
        self.c_dt = init_counter(self.M, self.T)
        self.c_k_token = init_counter(self.K)

        for d in range(self.M):
            self.k[d] = sample_uniform(self.K)
            self.c_k_doc[self.k[d]] += 1
            self.c_k_token[self.k[d]] += self.N[d]

            for n in range(self.N[d]):
                self.set_z(d, n, sample_uniform(self.T))

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
        self.set_z(d, n, sample_counts(counts))

    def prob_z(self, d, n, j):
        """
        ClusterLDA.prob_z(int, int, int): return float
        Returns the probability p(z_dn=j|w, z_-dn, alpha, beta)
        """

        prob = self.alpha + self.c_kt[self.k[d]][j]
        prob *= self.beta + self.c_tv[j][self.w[d][n]]
        prob /= self.Vbeta + self.c_t[j]
        return prob

    def set_z(self, d, n, z_dn):
        """
        ClusterLDA.set_z(int, int, int): return None
        Sets the value of z_dn and updates the counters. Does not adjust the
        counters for the previous value of z_dn.
        """

        self.z[d][n] = z_dn

        self.c_t[z_dn] += 1
        self.c_kt[self.k[d]][z_dn] += 1
        self.c_tv[z_dn][self.w[d][n]] += 1
        self.c_dt[d][z_dn] += 1

    def unset_z(self, d, n):
        """
        ClusterLDA.unset_z(int, int): return None
        Adjust the counters so that z_dn is not used in the counts.
        """

        self.c_t[self.z[d][n]] -= 1
        self.c_kt[self.k[d]][self.z[d][n]] -= 1
        self.c_tv[self.z[d][n]][self.w[d][n]] -= 1
        self.c_dt[d][self.z[d][n]] -= 1

    def sample_k(self, d):
        """
        ClusterLDA.sample_k(int): return None
        Samples the value of k_d according to the complete conditional
        """

        lcounts = []
        for j in range(self.K):
            self.set_k(d, j)
            lcounts.append(self.lprob_k(d, j))
        self.set_k(d, sample_lcounts(lcounts))

    def lprob_k(self, d, j):
        """
        ClusterLDA.lprob_k(int, int): return float
        Returns the log probability log p(k_d=j| w, z, k_-d, alpha, beta)
        """

        prob = math.log(self.gamma + self.c_k_doc[j] - 1)
        for t in set(self.z[d]):
            prob += math.lgamma(self.alpha + self.c_kt[j][t])
            prob -= math.lgamma(self.alpha + self.c_kt[j][t] - self.c_dt[d][t])
        prob += math.lgamma(self.Talpha + self.c_k_token[j] - self.N[d])
        prob -= math.lgamma(self.Talpha + self.c_k_token[j])
        return prob

    def set_k(self, d, k_d):
        """
        ClusterLDA.set_k(int, int): return None
        Updates the value k_d along with all counters. This method also adjust
        the counters related to the previous value of k_d
        """

        self.c_k_doc[self.k[d]] -= 1
        for z in self.z[d]:
            self.c_kt[self.k[d]][z] -= 1
        self.c_k_token[self.k[d]] -= self.N[d]

        self.k[d] = k_d

        self.c_k_doc[self.k[d]] += 1
        for z in self.z[d]:
            self.c_kt[self.k[d]][z] += 1
        self.c_k_token[self.k[d]] += self.N[d]

    def topic_words(self, t, n):
        """
        ClusterLDA.topic_words(int, int): return list of int
        Returns the top n words for topic t
        """

        return top_n(self.c_tv[t], n)

    def cluster_topics(self, k, n):
        """
        ClusterLDA.cluster_topics(int, int): return list of int
        Returns the top n topics for the cluster k
        """

        return top_n(self.c_kt[k], n)

    def doc_topics(self, d, n):
        """
        ClusterLDA.doc_topics(int, int): return int
        Returns the top n topics for the document d
        """

        return top_n(self.c_dt[d], n)

    def print_state(self, verbose=False):
        for k in range(self.K):
            print '{0} -'.format(k),
            for t in self.cluster_topics(k, 5):
                print '{0}('.format(t),
                for v in self.topic_words(t, 3):
                    print self.vocab[v],
                print ')',
            print

        if verbose:
            for t in range(self.T):
                print '{0} -'.format(t),
                for v in self.topic_words(t, 15):
                    print self.vocab[v],
                print

            for d in range(self.M):
                print '{0} ({1}) -'.format(self.titles[d], self.k[d]),
                for t in self.doc_topics(d, 5):
                    print '{0}('.format(t),
                    for v in self.topic_words(t, 2):
                        print self.vocab[v],
                    print ')',
                print
