"""Implements Mixture of Multinomials for document clustering"""

from __future__ import division

import math
from pytopic.model.basic import TopicModel
from pytopic.util.compute import sample_uniform, sample_lcounts, top_n
from pytopic.util.compute import lnormalize_list, ladd, lsum, digamma
from pytopic.util.compute import normalize_list
from pytopic.util.data import init_counter

def annealed_gibbs_mixmulti(model, temp):
    """
    annealed_gibbs_mixmulti(MixtureMultinomial, float): func
    Creates an annealed Gibbs sampler for the Mixture of Multinomials model
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

    def compute_lprob_k(d, j):
        prob = math.log(gamma + c_k_doc[j] - 1)
        for v in doc_words[d]:
            prob += math.lgamma(beta + c_kv[j][v])
            prob -= math.lgamma(beta + c_kv[j][v] - c_dv[d][v])
        prob += math.lgamma(Vbeta + c_k_token[j] - N[d])
        prob -= math.lgamma(Vbeta + c_k_token[j])
        return prob

    if temp == 1:
        lprob_k = compute_lprob_k
    else:
        lprob_k = lambda d, j: compute_lprob_k(d, j) / temp

    return sample_model


def gibbs_mixmulti(model):
    """
    gibbs_mixmulti(MixtureMultinomial): func
    Creates a Gibbs sampler for the mixture of multinomials model
    """

    return annealed_gibbs_mixmulti(model, 1)


def annealed_em_mixmulti(model, temp):
    """
    annealed_em_mixmulti(MixtureMultinomial, float): func
    Creates an annealed em inference algorithm for mixture of multinomials
    """

    M = range(model.M)
    K = range(model.K)
    V = range(model.V)
    w = model.c_dv

    doc_words = [set(doc) for doc in model.w]
    word_docs = [set() for v in V]
    for d in M:
        for word in doc_words[d]:
            word_docs[word].add(d)

    def init_posteriors():
        lambda_ = [math.log(model.c_k_doc[k]) for k in K]
        lnormalize_list(lambda_)

        phi = [[math.log(1 + model.c_kv[k][v]) for v in V] for k in K]
        for k in K:
            lnormalize_list(phi[k])

        return update_posteriors(lambda_, phi)

    if temp == 1:
        def update_posteriors(lambda_, phi):
            posteriors = [calc_posterior(d, lambda_, phi) for d in M]
            for posterior in posteriors:
                lnormalize_list(posterior)
            return posteriors
    else:
        def update_posteriors(lambda_, phi):
            posteriors = [calc_posterior(d, lambda_, phi) for d in M]
            for d in M:
                posteriors[d] = [post / temp for post in posteriors[d]]
                lnormalize_list(posteriors[d])
            return posteriors

    def calc_posterior(d, lambda_, phi):
        likes = [sum(w[d][v] * phi[k][v] for v in doc_words[d]) for k in K]
        return [prior + like for prior, like in zip(lambda_, likes)]

    def calc_lambda():
        lambda_ = [lsum(posteriors[d][k] for d in M) for k in K]
        lnormalize_list(lambda_)
        return lambda_

    def calc_phi():
        phi = [[0 for v in V] for k in K]
        for k in K:
            for v in V:
                for d in word_docs[v]:
                    pseudocount = posteriors[d][k] + math.log(w[d][v])
                    phi[k][v] = ladd(phi[k][v], pseudocount)
            lnormalize_list(phi[k])
        return phi

    def update_model():
        for d in M:
            model.set_k(d, max(K, key=lambda k: posteriors[d][k]))

    posteriors = init_posteriors()

    def em_iteration():
        lambda_ = calc_lambda()
        phi = calc_phi()
        posteriors[:] = update_posteriors(lambda_, phi)
        update_model()

    return em_iteration


def em_mixmulti(model):
    """
    em_mixmulti(MixtureMultinomial): func
    Creates an em inference algorithm for the mixture of multinomials model
    """

    return annealed_em_mixmulti(model, 1)


def vem_mixmulti(model):
    """
    vem_mixmulti(MixtureMultinomial): func
    Creates an variational em inference algorithm for the mixture of
    multinomials model
    """

    gamma = model.gamma
    beta = model.beta

    M = range(model.M)
    K = range(model.K)
    V = range(model.V)
    w = model.c_dv

    doc_words = [set(doc) for doc in model.w]
    word_docs = [set() for v in V]
    for d in M:
        for word in doc_words[d]:
            word_docs[word].add(d)

    def init_theta():
        lambda_ = [math.log(model.c_k_doc[k]) for k in K]
        lnormalize_list(lambda_)

        phi = [[math.log(1 + model.c_kv[k][v]) for v in V] for k in K]
        for k in K:
            lnormalize_list(phi[k])

        theta = [[lambda_[k] for k in K] for d in M]
        for d in M:
            for k in K:
                theta[d][k] += sum(w[d][v] * phi[k][v] for v in doc_words[d])
            lnormalize_list(theta[d])

        return theta

    # TODO annealing
    def update_theta(a, b):
        dig_suma = digamma(sum(a))
        dig_a = [digamma(a[k]) - dig_suma for k in K]

        dig_sumb = [digamma(sum(b[k])) for k in K]
        dig_b = [[digamma(b[k][v]) - dig_sumb[k] for v in V] for k in K]

        theta = [[dig_a[k] for k in K] for d in M]
        for d in M:
            for k in K:
                theta[d][k] += sum(w[d][v] * dig_b[k][v] for v in doc_words[d])
            lnormalize_list(theta[d])

        return theta

    def calc_a():
        a = [gamma for k in K]
        for k in K:
            a[k] += math.exp(lsum(theta[d][k] for d in M))
        normalize_list(a)
        return a

    def calc_b():
        b = [[beta for v in V] for k in K]
        for k in K:
            for v in V:
                for d in word_docs[v]:
                    b[k][v] += math.exp(theta[d][k]) * w[d][v]
            normalize_list(b[k])
        return b

    def update_model():
        for d in M:
            model.set_k(d, max(K, key=lambda k: theta[d][k]))

    theta = init_theta()

    def vem_iteration():
        a = calc_a()
        b = calc_b()
        theta[:] = update_theta(a, b)
        update_model()

    return vem_iteration


class MixtureMultinomial(TopicModel):
    """Implementation of Mixture of Multinomials with a Gibbs sampler"""

    algorithms = {'gibbs': gibbs_mixmulti,
                  'annealed gibbs': annealed_gibbs_mixmulti,
                  'em': em_mixmulti,
                  'annealed em': annealed_em_mixmulti,
                  'vem': vem_mixmulti}

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
