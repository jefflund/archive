"""Implementation of ClusterLDA, first described by Lund in 2011"""

import math
from topic.model import TopicModel
from util.sample import (sample_uniform, sample_order, sample_lcounts,
                         sample_counts)

class ClusterLDA(TopicModel):

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

    def iteration(self):
        for d in sample_order(self.M):
            self.sample_k(d)
            for n in sample_order(self.N[d]):
                self.sample_z(d, n)

    def sample_z(self, d, n):
        self.unset_z(d, n)
        counts = [self.prob_z(d, n, j) for j in range(self.T)]
        self.set_z(d, n, sample_counts(counts))

    def prob_z(self, d, n, j):
        prob = self.alpha + self.h[self.k[d]][j]
        prob *= self.beta + self.p[j][self.w[d][n]]
        prob /= self.Vbeta + self.s[j]
        return prob

    def set_z(self, d, n, z_dn):
        self.z[d][n] = z_dn

        self.s[z_dn] += 1
        self.h[self.k[d]][z_dn] += 1
        self.p[z_dn][self.w[d][n]] += 1
        self.r[d][z_dn] += 1

    def unset_z(self, d, n):
        self.s[self.z[d][n]] -= 1
        self.h[self.k[d]][self.z[d][n]] -= 1
        self.p[self.z[d][n]][self.w[d][n]] -= 1
        self.r[d][self.z[d][n]] -= 1

    def sample_k(self, d):
        lcounts = []
        for j in range(self.K):
            self.set_k(d, j)
            lcounts.append(self.lprob_k(d, j))
        self.set_k(d, sample_lcounts(lcounts))

    def lprob_k(self, d, j):
        prob = math.log(self.gamma + self.l[j] - 1)
        for t in set(self.z[d]):
            prob += math.gamma(self.alpha + self.h[j][t])
            prob -= math.gamma(self.alpha + self.h[j][t] - self.r[d][t])
        prob += math.gamma(self.Talpha + self.q[j] - self.N[d])
        prob -= math.gamma(self.Talpha + self.q[j])
        return prob

    def set_k(self, d, k_d):
        self.l[self.k[d]] -= 1
        for z in self.z[d]:
            self.h[self.k[d]][z] -= 1
        self.q[self.k[d]] -= self.N[d]

        self.k[d] = k_d

        self.l[self.k[d]] += 1
        for z in self.z[d]:
            self.h[self.k[d]][z] += 1
        self.q[self.k[d]] += self.N[d]
