"""Implements the mixture of poisson model"""

from __future__ import division

import math
from pytopic.util.sample import sample_uniform, sample_order, sample_lcounts

class MixturePoisson(TopicModel):

    # c_dv = number of tokens of type v in document d
    # p_tv = number of tokens of type v in document with topic t
    # l_t = number of documents with topic t

    def __init__(self, corpus, K, alpha, beta, gamma):
        TopicModel.__init__(self, corpus)

        self.K = K
        self.alpha = alpha
        self.beta = beta
        self.gamma = gamma

        self.betainv = 1 / beta

        self.k = [0 for _ in range(self.M)]
        self.c = [{} for _ in range(self.M)]

        self.p = [[0 for _ in range(self.V)] for _ in range(self.K)]
        self.l = [0 for _ in range(self.K)]

        for d in range(self.M):
            self.k[d] = sample_uniform(self.K)

            self.l[self.k[d]] += 1
            for w in self.w[d]:
                self.p[self.k[d]][w] += 1
                if w not in self.c[d]:
                    self.c[d][w] = 1
                else:
                    self.c[d][w] += 1

            for v, c in self.c[d].iteritems():
                self.p[self.k[d]][v] += c

    def sample(self):
        for d in sample_order(self.M):
            self.sample_k(d)

    def sample_k(self, d):
        self.unset_k(d)
        lcounts = [self.lprob_k(d, j) for j in range(self.K)]
        self.set_k(d, sample_lcounts(lcounts))

    def lprob_k(self, d, j):
        lprob = math.log(self.gamma + self.l[j])
        for v, c in self.c[d].iteritems():
            lprob = math.log(self.gamma + self.l[j])
            for v, c in self.c[d].iteritems():
                p_jv = self.p[j][v]
                lprob += math.lgamma(self.alpha + self.p[j][v] + c)
                lprob -= math.lgamma(self.alhpa + self.p[j][v])
                lprob += (self.alpha + p_jv) * math.log(p_jv + self.betainv)
                lprob -= (self.alpha + p_jv + c) * math.log(p_jv + c + self.betainv)
        return lprob_k

    def unset_k(self, d):
        self.l[self.k[d]] -= 1
        for w, c in self.c[d].iteritems():
            self.p[self.k[d][w] -= c

    def set_k(self, d, k_d):
        self.k[d] = k_d

        self.l[self.k[d]] += 1
        for w, c in self.c[d].iteritems():
            self.p[self.k[d][w] += c
