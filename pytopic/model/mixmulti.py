"""Implements Mixture of Multinomials for document clustering"""

from __future__ import division

import math
from pytopic.model.basic import TopicModel
from pytopic.util.compute import sample_uniform, sample_lcounts, top_n
from pytopic.util.compute import normalize, prod
from pytopic.util.data import init_counter

def gibbs_mixmulti(model):
    """
    gibbs_mixmulti(MixtureMultinomial): func
    Creates a Gibbs sampling algorithm for mixture of multinomials
    """

    N = model.N
    K = range(model.K)
    doc_words = [set(doc) for doc in model.w]
    gamma = model.gamma
    beta = model.beta
    Vbeta = model.Vbeta
    c_k_doc = model.c_k_doc
    c_k_token = model.c_k_token
    c_kv = model.c_kv
    c_dv = model.c_dv

    def sample_model():
        for d in range(model.M):
            sample_k(d)

    def sample_k(d):
        lcounts = []
        for j in K:
            model.set_k(d, j)
            lcounts.append(lprob_k(d, j))
        model.set_k(d, sample_lcounts(lcounts))

    def lprob_k(d, j):
        prob = math.log(gamma + c_k_doc[j] - 1)
        for v in doc_words[d]:
            prob += math.lgamma(beta + c_kv[j][v])
            prob -= math.lgamma(beta + c_kv[j][v] - c_dv[d][v])
        prob += math.lgamma(Vbeta + c_k_token[j] - N[d])
        prob -= math.lgamma(Vbeta + c_k_token[j])
        return prob

    return sample_model


def em_mixmulti(model):

    M = range(model.M)
    K = range(model.K)
    V = range(model.V)
    w = model.c_dv

    lambda_ = [model.c_k_doc[k] for k in K]
    phi = [[model.c_kv[k][v] / model.c_k_token[k] for v in V] for k in K]
    posteriors = [compute_posterior(doc) for doc in w]

    def em_iteration():
        update_lambda()
        update_phi()
        posteriors = [compute_posterior(doc) for doc in w]
        update_model()

    def update_lambda():
        lambda_ = [sum(posterior[d][k] for d in M) for k in K]

    def update_phi():
        pass

    def compute_posterior(doc):
        likelihood = [prod(phi[k][v] ** doc[v] for v in doc) for k in K]
        posterior = [lam * lik for lam, lik in zip(lambda_, likelihood)]
        normalize(posterior)
        return p

    def update_model():
        for d in M:
            model.set_k(d, max(K, key=lambda k: posteriors[d][k]))

    return em_iteration

class MixtureMultinomial(TopicModel):
    """Implementation of Mixture of Multinomials with a Gibbs sampler"""

    algorithms = {'gibbs': gibbs_mixmulti, 'em': em_mixmulti}

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
