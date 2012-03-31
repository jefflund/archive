"""Implementation of vanilla LDA as described by Blei in 2003"""

from topic.model import TopicModel
from util.sample import sample_uniform, sample_counts

class VanillaLDA(TopicModel):

    def __init__(self, corpus, T, alpha, beta):
        TopicModel.__init__(self, corpus)

        self.T = T
        self.alpha = alpha
        self.beta = beta

        self.z = [[0 for _ in size] for size in self.N]

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
        self.unset_z(d, n)
        counts = [self.prob_z(d, n, j) for j in range(self.T)]
        self.set_z(d, n, sample_counts(counts))

    def set_z(self, d, n, z_dn):

        self.z[d][n] = z_dn

        self.s[z_dn] += 1
        self.h[d][z_dn] += 1
        self.p[z_dn][self.w[d][n]] += 1

    def unset_z(self, d, n):

        self.s[self.z[d][n]] += 1
        self.h[d][self.z[d][n]] += 1
        self.p[self.z[d][n]][self.w[d][n]] += 1
