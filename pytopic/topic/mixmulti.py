"""Implements the mixture of multinomials model"""

import math
from topic.model import TopicModel, top_n
from util.sample import sample_uniform, sample_order, sample_lcounts

class MixtureMultinomial(TopicModel):
    """Implementation of Mixture of Multinomials with a Gibbs sampler"""

    def __init__(self, corpus, K, gamma, beta):
        TopicModel.__init__(self, corpus)

        self.K = K
        self.gamma = gamma
        self.beta = beta

        self.Vbeta = self.V * self.beta

        self.k = [0 for _ in range(self.M)]

        self.l = [0 for _ in range(self.K)]
        self.p = [[0 for _ in range(self.V)] for _ in range(self.K)]
        self.r = [[0 for _ in range(self.V)] for _ in range(self.M)]
        self.q = [0 for _ in range(self.K)]

        for d in range(self.M):
            self.k[d] = sample_uniform(self.K)

            self.l[self.k[d]] += 1
            self.q[self.k[d]] += self.N[d]
            for w in self.w[d]:
                self.p[self.k[d]][w] += 1
                self.r[d][w] += 1

        self.doc_words = [set(self.w[d]) for d in range(self.M)]

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

        prob = math.log(self.gamma + self.l[j] - 1)
        for v in self.doc_words[d]:
            prob += math.lgamma(self.beta + self.p[j][v])
            prob += math.lgamma(self.beta + self.p[j][v] - self.r[d][v])
        prob += math.lgamma(self.Vbeta + self.q[j] - self.N[d])
        prob -= math.lgamma(self.Vbeta + self.q[j])
        return prob

    def set_k(self, d, k_d):
        """
        MixtureMultinomial.set_k(int, int): return None
        Updates the value k_d along with all counters. This method also adjust
        the counters related to the previous value of k_d
        """

        self.l[self.k[d]] -= 1
        self.q[self.k[d]] -= self.N[d]
        for w in self.w[d]:
            self.p[self.k[d]][w] -= 1

        self.k[d] = k_d

        self.l[k_d] += 1
        self.q[k_d] += self.N[d]
        for w in self.w[d]:
            self.p[self.k[d]][w] += 1

    def cluster_words(self, k, n):
        """
        MixtureMultinomial.cluster_topics(int, int): return list of int
        Returns the top n words for the cluster k
        """

        return top_n(self.p[k], n)

    def print_state(self, verbose=False):
        for k in range(self.K):
            print '{} -'.format(k),
            for v in self.cluster_words(k, 10):
                print self.vocab[v],
            print

        if verbose:
            for d in range(self.M):
                print '{} - {}'.format(self.titles[d], self.k[d])
