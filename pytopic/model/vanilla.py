from __future__ import division

from pytopic.model import basic
from pytopic.util import compute, data

def _gibbs(model, temp, sample_func):
    alpha = model.alpha
    beta = model.beta
    Vbeta = model.beta
    w = model.w
    c_dt = model.c_dt
    c_tv = model.c_tv
    c_t = model.c_t

    def sample_model():
        for d in range(model.M):
            for n in range(model.N[d]):
                sample_z(d, n)

    def sample_z(d, n):
        model.unset_z(d, n)
        counts = [prob_z(d, n, j) for j in range(model.T)]
        model.set_z(d, n, sample_func(counts))

    def compute_prob_z(d, n, j):
        prob = alpha + c_dt[d][j]
        prob *= beta + c_tv[j][w[d][n]]
        prob /= Vbeta + c_t[j]
        return prob

    if temp == 1:
        prob_z = compute_prob_z
    else:
        exponent = 1 / temp
        prob_z = lambda d, n, j: compute_prob_z(d, n, j) ** exponent

    return sample_model


def gibbs(model):
    return _gibbs(model, 1, compute.sample_counts)


def annealed_gibbs(model, temp):
    return _gibbs(model, temp, compute.sample_counts)


def ccm(model):
    return _gibbs(model, 1, compute.argmax)


class VanillaLDA(basic.TopicModel):
    """Latent Dirichlet Allocation with a Gibbs sampler"""

    algorithms = {'gibbs': gibbs,
                  'annealed gibbs': annealed_gibbs,
                  'ccm': ccm}
    default_algorithm = 'ccm'

    def __init__(self, corpus, T, alpha, beta):
        basic.TopicModel.__init__(self, corpus)

        self.T = T
        self.alpha = alpha
        self.beta = beta
        self.Vbeta = self.V * self.beta

        self.z = [[0 for _ in range(size)] for size in self.N]

        self.c_t = data.init_counter(self.T)
        self.c_dt = data.init_counter(self.M, self.T)
        self.c_tv = data.init_counter(self.T, self.V)

        for d in range(self.M):
            for n in range(self.N[d]):
                self.set_z(d, n, compute.sample_uniform(self.T))

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

        return compute.top_n(self.c_tv[t], n)

    def doc_topics(self, d, n):
        """
        VanillaLDA.doc_topics(int, int): return list of int
        Returns the top n topics in document d
        """

        return compute.top_n(self.c_dt[d], n)

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
