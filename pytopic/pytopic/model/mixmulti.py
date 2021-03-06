"""Implements Mixture of Multinomials for document clustering"""

from __future__ import division

import collections
import math
import random
from pytopic.model import basic
from pytopic.util import compute, data

def _gibbs(model, temp, sample_func):
    N = model.N
    K = range(model.K)
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
        model.set_k(d, sample_func(lcounts))

    def compute_lprob_k(d, j):
        prob = math.log(gamma + c_k_doc[j] - 1)
        for v in c_dv[d]:
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


def gibbs(model):
    """
    gibbs(MixtureMultinomial): func
    Creates a Gibbs sampler for the mixture of multinomials model
    """

    return _gibbs(model, 1, compute.sample_lcounts)


def annealed_gibbs(model, temp):
    """
    annealed_gibbs(MixtureMultinomial, float): func
    Creates an annealed Gibbs sampler for the Mixture of Multinomials model
    """

    return _gibbs(model, temp, compute.sample_lcounts)


def ccm(model):
    """
    ccm(model): func
    Returns an complete conditional maximization algorithm for the Mixture
    of Multinomials model
    """

    return _gibbs(model, 1, compute.argmax)


def annealed_em(model, temp):
    """
    annealed_em(MixtureMultinomial, float): func
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
        lambda_ = [math.log(1 + model.c_k_doc[k]) for k in K]
        compute.lnormalize(lambda_)

        phi = [[math.log(1 + model.c_kv[k][v]) for v in V] for k in K]
        for k in K:
            compute.lnormalize(phi[k])

        return update_posteriors(lambda_, phi)

    if temp == 1:
        def update_posteriors(lambda_, phi):
            posteriors = [calc_posterior(d, lambda_, phi) for d in M]
            for posterior in posteriors:
                compute.lnormalize(posterior)
            return posteriors
    else:
        def update_posteriors(lambda_, phi):
            posteriors = [calc_posterior(d, lambda_, phi) for d in M]
            for d in M:
                posteriors[d] = [post / temp for post in posteriors[d]]
                compute.lnormalize(posteriors[d])
            return posteriors

    def calc_posterior(d, lambda_, phi):
        likes = [sum(w[d][v] * phi[k][v] for v in doc_words[d]) for k in K]
        return [prior + like for prior, like in zip(lambda_, likes)]

    def calc_lambda():
        lambda_ = [compute.lsum(posteriors[d][k] for d in M) for k in K]
        compute.lnormalize(lambda_)
        return lambda_

    def calc_phi():
        phi = [[0 for v in V] for k in K]
        for k in K:
            for v in V:
                for d in word_docs[v]:
                    pseudocount = posteriors[d][k] + math.log(w[d][v])
                    phi[k][v] = compute.ladd(phi[k][v], pseudocount)
            compute.lnormalize(phi[k])
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


def em(model):
    """
    em(MixtureMultinomial): func
    Creates an em inference algorithm for the mixture of multinomials model
    """

    return annealed_em(model, 1)


def annealed_vem(model, temp):
    """
    annealed_vem(MixtureMultinomial): func
    Creates an annealed variational em inference algorithm for the mixture of
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
        lambda_ = [math.log(1 + model.c_k_doc[k]) for k in K]
        compute.lnormalize(lambda_)

        phi = [[math.log(1 + model.c_kv[k][v]) for v in V] for k in K]
        for k in K:
            compute.lnormalize(phi[k])

        theta = [[lambda_[k] for k in K] for d in M]
        for d in M:
            for k in K:
                theta[d][k] += sum(w[d][v] * phi[k][v] for v in doc_words[d])
            compute.lnormalize(theta[d])

        return theta

    def calc_theta(a, b):
        dig_suma = compute.digamma(sum(a))
        dig_a = [compute.digamma(a[k]) - dig_suma for k in K]

        dig_sumb = [compute.digamma(sum(b[k])) for k in K]
        dig_b = [[compute.digamma(b[k][v]) - dig_sumb[k] for v in V]
                 for k in K]

        theta = [[dig_a[k] for k in K] for d in M]
        for d in M:
            for k in K:
                theta[d][k] += sum(w[d][v] * dig_b[k][v] for v in doc_words[d])

        return theta

    if temp == 1:
        def update_theta(a, b):
            theta = calc_theta(a, b)
            for d in M:
                compute.lnormalize(theta[d])
            return theta
    else:
        def update_theta(a, b):
            theta = calc_theta(a, b)
            for d in M:
                theta[d] = [t / temp for t in theta[d]]
                compute.lnormalize(theta[d])
            return theta

    def calc_a():
        a = [gamma for k in K]
        for k in K:
            a[k] += math.exp(compute.lsum(theta[d][k] for d in M))
        compute.normalize(a)
        return a

    def calc_b():
        b = [[beta for v in V] for k in K]
        for k in K:
            for v in V:
                for d in word_docs[v]:
                    b[k][v] += math.exp(theta[d][k]) * w[d][v]
            compute.normalize(b[k])
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


def vem(model):
    """
    vem(MixtureMultinomial): func
    Creates an variational em inference algorithm for the mixture of
    multinomials model
    """

    return annealed_vem(model, 1)


class MixtureMultinomial(basic.TopicModel):
    """Implementation of Mixture of Multinomials model"""

    algorithms = {'gibbs': gibbs,
                  'annealed gibbs': annealed_gibbs,
                  'ccm': ccm,
                  'em': em,
                  'annealed em': annealed_em,
                  'vem': vem,
                  'annealed vem': annealed_vem}
    default_algorithm = 'ccm'

    def __init__(self, corpus, K, gamma, beta):
        basic.TopicModel.__init__(self, corpus)

        self.K = K
        self.gamma = gamma
        self.beta = beta

        self.Vbeta = self.V * self.beta

        self.k = [0 for _ in range(self.M)]

        self.c_k_doc = data.init_counter(self.K)
        self.c_kv = data.init_counter(self.K, self.V)
        self.c_dv = [collections.Counter() for d in range(self.M)]
        self.c_k_token = data.init_counter(self.K)

        for d in range(self.M):
            self.k[d] = compute.sample_uniform(self.K)

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

    def perplexity(self, corpus):
        """
        MixtureMultinomial.perplexity(Corpus): float
        Computes the held-out perplexity for the provided Corpus given this
        model, which is defined as exp(p), where p is the entropy. This
        measures how well the model predicts the provided Corpus.
        """

        K = range(self.K)

        lambda_ = [math.log(self.gamma + c) for c in self.c_k_doc]
        compute.lnormalize(lambda_)

        phi = [[math.log(self.beta + c) for c in c_k] for c_k in self.c_kv]
        for phi_k in phi:
            compute.lnormalize(phi_k)

        docs = [collections.Counter(doc) for doc in corpus]

        p = 0
        for w in docs:
            posts = (lambda_[k] + sum(w[v] * phi[k][v] for v in w) for k in K)
            p += compute.lsum(posts)
        p /= sum(len(doc) for doc in corpus)

        return math.exp(-p)

    def likelihood(self):
        """
        MixtureMultinomial.likelihood(): float
        Computes the unnormalized log likelihood of the data, which gives an
        indication as to how strongly the model represents the training data.
        """

        K = range(self.K)
        V = range(self.V)

        lambda_ = [math.log(self.gamma + self.c_k_doc[k]) for k in K]
        compute.lnormalize(lambda_)

        phi = [[math.log(self.beta + self.c_kv[k][v]) for v in V] for k in K]
        for phi_k in phi:
            compute.lnormalize(phi_k)

        like = 0
        for d, k_d in enumerate(self.k):
            like += lambda_[k_d]
            like += sum(self.c_dv[d][v] * phi[k_d][v] for v in self.c_dv[d])
        return like
