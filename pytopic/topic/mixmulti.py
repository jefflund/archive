"""Implements the mixture of multinomials model"""

import math
from pytopic.topic.model import TopicModel
from pytopic.util.compute import (sample_uniform, sample_order, sample_lcounts,
                                  top_n)
from pytopic.util.data import init_counter

class MixtureMultinomial(TopicModel):
    """Implementation of Mixture of Multinomials with a Gibbs sampler"""

    def __init__(self, corpus, K, gamma, beta):
        TopicModel.__init__(self, corpus)

        self.K = K
        self.gamma = gamma
        self.beta = beta

        self.Vbeta = self.V * self.beta

        self.k = [0 for _ in range(self.M)]

        self.c_k_doc = init_counter(self.K)
        self.c_kv = init_counter(self.K, self.V)
        self.c_dv = init_counter(self.M, self.V)
        self.c_k_token = init_counter(self.K)

        for d in range(self.M):
            self.k[d] = sample_uniform(self.K)

            self.c_k_doc[self.k[d]] += 1
            self.c_k_token[self.k[d]] += self.N[d]
            for w in self.w[d]:
                self.c_kv[self.k[d]][w] += 1
                self.c_dv[d][w] += 1

    def sample(self):
        for d in sample_order(self.M):
            self.sample_k(d)

    def sample_k(self, d):
        """
        MixtureMultinomial.sample_k(int): return None
        Samples the value of k_d according to the complete conditional
        """

        lcounts = []
        for j in range(self.K):
            self.set_k(d, j)
            lcounts.append(self.lprob_k(d, j))
        self.set_k(d, sample_lcounts(lcounts))

    def lprob_k(self, d, j):
        """
        MixtureMultinomial.lprob_k(int, int): return float
        Returns the log probability log p(k_d=j| w, z, k_-d, alpha, beta)
        """

        prob = math.log(self.gamma + self.c_k_doc[j] - 1)
        for v in set(self.w[d]):
            prob += math.lgamma(self.beta + self.c_kv[j][v])
            prob += math.lgamma(self.beta + self.c_kv[j][v] - self.c_dv[d][v])
        prob += math.lgamma(self.Vbeta + self.c_k_token[j] - self.N[d])
        prob -= math.lgamma(self.Vbeta + self.c_k_token[j])
        return prob

    def set_k(self, d, k_d):
        """
        MixtureMultinomial.set_k(int, int): return None
        Updates the value k_d along with all counters. This method also adjust
        the counters related to the previous value of k_d
        """

        self.c_k_doc[self.k[d]] -= 1
        self.c_k_token[self.k[d]] -= self.N[d]
        for w in self.w[d]:
            self.c_kv[self.k[d]][w] -= 1

        self.k[d] = k_d

        self.c_k_doc[k_d] += 1
        self.c_k_token[k_d] += self.N[d]
        for w in self.w[d]:
            self.c_kv[self.k[d]][w] += 1

    def cluster_words(self, k, n):
        """
        MixtureMultinomial.cluster_topics(int, int): return list of int
        Returns the top n words for the cluster k
        """

        return top_n(self.c_kv[k], n)

    def print_state(self, verbose=False):
        for k in range(self.K):
            print '{0} -'.format(k),
            for v in self.cluster_words(k, 10):
                print self.vocab[v],
            print

        if verbose:
            for d in range(self.M):
                print '{0} - {1}'.format(self.titles[d], self.k[d])
