"""Implements Mixture of Multinomials for document clustering"""

from __future__ import division

import collections
import math
import random
from pytopic.model.basic import TopicModel
from pytopic.util.compute import sample_uniform, sample_lcounts, top_n
from pytopic.util.compute import lnormalize, ladd, lsum, digamma
from pytopic.util.compute import normalize, argmax
from pytopic.util.data import init_counter

def _gibbs(model, temp, use_argmax):
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
        model.set_k(d, sample(lcounts))

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

    if use_argmax:
        sample = argmax
    else:
        sample = sample_lcounts

    return sample_model


def gibbs(model):
    """
    gibbs(MixtureMultinomial): func
    Creates a Gibbs sampler for the mixture of multinomials model
    """

    return _gibbs(model, 1, False)


def annealed_gibbs(model, temp):
    """
    annealed_gibbs(MixtureMultinomial, float): func
    Creates an annealed Gibbs sampler for the Mixture of Multinomials model
    """

    return _gibbs(model, temp, False)


def ecm(model):
    """
    ecm(model): func
    Returns an expectation conditional maximization algorithm for the Mixture
    of Multinomials model
    """

    return _gibbs(model, 1, True)


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
        lnormalize(lambda_)

        phi = [[math.log(1 + model.c_kv[k][v]) for v in V] for k in K]
        for k in K:
            lnormalize(phi[k])

        return update_posteriors(lambda_, phi)

    if temp == 1:
        def update_posteriors(lambda_, phi):
            posteriors = [calc_posterior(d, lambda_, phi) for d in M]
            for posterior in posteriors:
                lnormalize(posterior)
            return posteriors
    else:
        def update_posteriors(lambda_, phi):
            posteriors = [calc_posterior(d, lambda_, phi) for d in M]
            for d in M:
                posteriors[d] = [post / temp for post in posteriors[d]]
                lnormalize(posteriors[d])
            return posteriors

    def calc_posterior(d, lambda_, phi):
        likes = [sum(w[d][v] * phi[k][v] for v in doc_words[d]) for k in K]
        return [prior + like for prior, like in zip(lambda_, likes)]

    def calc_lambda():
        lambda_ = [lsum(posteriors[d][k] for d in M) for k in K]
        lnormalize(lambda_)
        return lambda_

    def calc_phi():
        phi = [[0 for v in V] for k in K]
        for k in K:
            for v in V:
                for d in word_docs[v]:
                    pseudocount = posteriors[d][k] + math.log(w[d][v])
                    phi[k][v] = ladd(phi[k][v], pseudocount)
            lnormalize(phi[k])
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
        lnormalize(lambda_)

        phi = [[math.log(1 + model.c_kv[k][v]) for v in V] for k in K]
        for k in K:
            lnormalize(phi[k])

        theta = [[lambda_[k] for k in K] for d in M]
        for d in M:
            for k in K:
                theta[d][k] += sum(w[d][v] * phi[k][v] for v in doc_words[d])
            lnormalize(theta[d])

        return theta

    def calc_theta(a, b):
        dig_suma = digamma(sum(a))
        dig_a = [digamma(a[k]) - dig_suma for k in K]

        dig_sumb = [digamma(sum(b[k])) for k in K]
        dig_b = [[digamma(b[k][v]) - dig_sumb[k] for v in V] for k in K]

        theta = [[dig_a[k] for k in K] for d in M]
        for d in M:
            for k in K:
                theta[d][k] += sum(w[d][v] * dig_b[k][v] for v in doc_words[d])

        return theta

    if temp == 1:
        def update_theta(a, b):
            theta = calc_theta(a, b)
            for d in M:
                lnormalize(theta[d])
            return theta
    else:
        def update_theta(a, b):
            theta = calc_theta(a, b)
            for d in M:
                theta[d] = [t / temp for t in theta[d]]
                lnormalize(theta[d])
            return theta

    def calc_a():
        a = [gamma for k in K]
        for k in K:
            a[k] += math.exp(lsum(theta[d][k] for d in M))
        normalize(a)
        return a

    def calc_b():
        b = [[beta for v in V] for k in K]
        for k in K:
            for v in V:
                for d in word_docs[v]:
                    b[k][v] += math.exp(theta[d][k]) * w[d][v]
            normalize(b[k])
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

def ga(model, pop_size, eliteness, mutate_prob, keep_elite):
    """
    ga(MixtureMultinomial, int, float, float, bool): func
    Creates a genetic algorithm inference algorithm for mixture of multinomials
    """

    gamma = model.gamma
    beta = model.beta

    K = range(model.K)
    M = range(model.M)
    V = range(model.V)
    doc_words = [set(doc) for doc in model.w]
    w = model.c_dv

    def init_gene(index):
        if index == 0:
            return [k for k in model.k]
        else:
            return [sample_uniform(len(K)) for _ in M]

    def mutate(gene):
        for d in M:
            if random.random() < mutate_prob:
                gene[d] = sample_uniform(len(K))

    def cross_over(parent_a, parent_b):
        parents = zip(parent_a, parent_b)

        select = [int(random.getrandbits(1)) for _ in M]
        child_a = [p[i] for p, i in zip(parents, select)]

        select = [(i - 1) * -1 for i in select]
        child_b = [p[(i - 1)] for p, i in zip(parents, select)]

        return child_a, child_b

    def reproduce(parent_a, parent_b):
        child_a, child_b = cross_over(parent_a, parent_b)
        mutate(child_a)
        mutate(child_b)
        return child_a, child_b

    def evaluate(gene):
        c_k = collections.Counter(gene)
        c_kv = [collections.Counter() for k in K]
        for d in M:
            for v in doc_words[d]:
                c_kv[gene[d]][v] += w[d][v]

        lambda_ = [math.log(gamma + c_k[k]) for k in K]
        lnormalize(lambda_)

        phi = [[math.log(beta + c_kv[k][v]) for v in V] for k in K]
        for phi_k in phi:
            lnormalize(phi_k)

        fitness = 0
        for d, k_d in enumerate(gene):
            fitness += lambda_[k_d]
            fitness += sum(w[d][v] * phi[k_d][v] for v in doc_words[d])
        return fitness

    def update_model(gene):
        for d, k_d in enumerate(gene):
            model.set_k(d, k_d)

    population = [init_gene(i) for i in range(pop_size)]
    elite_size = int(pop_size * eliteness)

    def generation():
        fitness = [evaluate(gene) for gene in population]
        update_model(population[argmax(fitness)])
        elite = top_n(fitness, elite_size, False)

        new_population = [population[e] for e in elite] if keep_elite else []
        while len(new_population) < pop_size:
            parent_a = population[random.choice(elite)]
            parent_b = population[random.choice(elite)]
            new_population.extend(reproduce(parent_a, parent_b))
        population[:] = new_population

    return generation

def default_ga(model):
    return ga(model, 100, .1, .01, False)


class MixtureMultinomial(TopicModel):
    """Implementation of Mixture of Multinomials with a Gibbs sampler"""

    algorithms = {'gibbs': gibbs,
                  'annealed gibbs': annealed_gibbs,
                  'ecm': ecm,
                  'em': em,
                  'annealed em': annealed_em,
                  'vem': vem,
                  'annealed vem': annealed_vem,
                  'ga': default_ga,
                  'custom ga': ga}

    def __init__(self, corpus, K, gamma, beta):
        TopicModel.__init__(self, corpus)

        self.K = K
        self.gamma = gamma
        self.beta = beta

        self.Vbeta = self.V * self.beta

        self.k = [0 for _ in range(self.M)]

        self.c_k_doc = init_counter(self.K)
        self.c_kv = init_counter(self.K, self.V)
        self.c_dv = [collections.Counter() for d in range(self.M)]
        self.c_k_token = init_counter(self.K)

        for d in range(self.M):
            self.k[d] = sample_uniform(self.K)

            self.c_k_doc[self.k[d]] += 1
            self.c_k_token[self.k[d]] += self.N[d]
            for w in self.w[d]:
                self.c_kv[self.k[d]][w] += 1
                self.c_dv[d][w] += 1

    def reinitialize(self):
        for d in range(self.M):
            self.set_k(d, sample_uniform(self.K))

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
        K = range(self.K)

        lambda_ = [math.log(self.gamma + c) for c in self.c_k_doc]
        lnormalize(lambda_)

        phi = [[math.log(self.beta + c) for c in c_k] for c_k in self.c_kv]
        for phi_k in phi:
            lnormalize(phi_k)

        docs = [collections.Counter(doc) for doc in corpus]

        p = 0
        for w in docs:
            p += lsum(lambda_[k] + sum(w[v] * phi[k][v] for v in w) for k in K)
        p /= sum(len(doc) for doc in corpus)

        return math.exp(-p)
