"""Utilities for sampling from various distributions"""

from __future__ import division

import math
import random

def sample_uniform(dim):
    """
    sample_uniform(int): return int
    Returns a random integer from 0 to the given dim (exclusive)
    """

    return random.randrange(dim)


def sample_counts(counts):
    """
    sample_counts(list of float): return int
    Returns an integer index sampled proportional to the given counts
    """

    sample = random.uniform(0, sum(counts))
    for key, count in enumerate(counts):
        if sample < count:
            return key
        sample -= count

    raise ValueError(counts)


def sample_lcounts(lcounts):
    """
    sample_lcounts(list of float): return int
    Returns an integer index sampled proportional to the given log counts
    """

    sample = math.log(random.random()) + lsum(lcounts)
    lcurr = float('-inf')
    for key, lcount in enumerate(lcounts):
        lcurr = ladd(lcurr, lcount)
        if sample < lcurr:
            return key

    raise ValueError()


def sample_order(dim):
    """
    sample_order(int): return list of int
    Returns the range up to the given dimension, shuffled for sampling
    """

    order = range(dim)
    random.shuffle(order)
    return order


def ladd(log_x, log_y):
    """
    ladd(float, float): return float
    Performs addition in log space
    """

    if log_y > log_x:
        log_x, log_y = log_y, log_x # log_x smaller gives more precision
    if math.isinf(log_x):
        return log_x
    return log_x + math.log(1 + math.exp(log_y - log_x))


def lsum(lcounts):
    """
    lsum(list of float): float
    Returns the log sum of the log space counts
    """

    ltotal = float('-inf')
    for lcount in lcounts:
        ltotal = ladd(ltotal, lcount)
    return ltotal


def n_choose_2(n):
    """
    n_choose_2(int): return int
    Returns n choose 2, or n(n-1)/2.
    """

    return (n * (n - 1)) / 2


def normalize(counts):
    """
    normalize(list of float): None
    Normalizes the count list
    """

    total = sum(counts)
    for i in range(len(counts)):
        counts[i] /= total


def lnormalize(counts):
    """
    lnormalize(list of float): None
    Normalizes the count list in log space
    """

    lsum = float('-inf')
    for count in counts:
        lsum = ladd(lsum, count)
    for i in range(len(counts)):
        counts[i] -= lsum


def top_n(counts, n, only_positive=True):
    """
    top_n(list of int, int): return list of int
    Returns the indices of the top n counts in the given list of counts.
    If only_positive is True, then only counts greater than 0 are included.
    """

    keys = range(len(counts))
    if only_positive:
        keys = [i for i in keys if counts[i] > 0]
    return sorted(keys, key=lambda x: counts[x], reverse=True)[:n]


def lim_plogp(p):
    """
    lim_plogp(float): float
    Returns the limit of p log p as p approaches the given value.
    When p approaches 0, then this is 0. Otherwise, it is simply p log p.
    """

    try:
        return p * math.log(p)
    except ValueError:
        if p == 0:
            return 0
        else:
            raise


def lim_xlogy(x, y):
    """
    lim_xlogy(float, float): float
    Returns the limit of x log y as x and y approach the given values.
    For cases involving 0 the limit is 0, except when only y approaches 0.
    Otherwise, the limit is simply x log y.
    """

    try:
        return x * math.log(y)
    except ValueError:
        if x == 0:
            return 0
        else:
            raise

# both the digamma and trigamma algorithms are ported from Apache Commons,
# which is released under the Apache 2.0 license, just like PyTopic

def digamma(x):
    """
    digamma(float): float
    Computes the digamma function, which is the first derivative of the log
    gamma function. Note that this algorithm is inefficient for large negative
    values of x.
    """

    if 0 < x <= 1e-5:
        return -.577215664901532860606512090082 - 1 / x
    elif x >= 49:
        inv = 1/ (x * x)
        lx = math.log(x)
        return lx - .5 / x - inv * ((1 / 12) + inv * (1 / 120 - inv / 252))
    else:
        return digamma(x + 1) - 1 / x


def trigamma(x):
    """
    trigamma(float): float
    Computes the trigamma function, which is the second derivative of the log
    gamma function. Note that this algorithm is inefficient for large negative
    values of x.
    """

    inv = 1 / (x * x)
    if 0 < x <= 1e-5:
        return inv
    elif x >= 49:
        return 1 / x + inv / 2 + inv / x * (1 / 6 - inv * (1 / 30 + inv / 42))
    else:
        return trigamma(x + 1) + inv


def argmax(counts):
    """
    argmax(list of float): int
    Returns the index of the highest count in the list
    """

    return max(range(len(counts)), key=lambda i: counts[i])
