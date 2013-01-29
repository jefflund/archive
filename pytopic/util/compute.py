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

    lsum = float('-inf')
    for lcount in lcounts:
        lsum = ladd(lsum, lcount)

    sample = math.log(random.random()) + lsum
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
        log_x, log_y = log_y, log_x
    if math.isinf(log_x):
        return log_x
    return log_x + math.log(1 + math.exp(log_y - log_x))


def n_choose_2(n):
    """
    n_choose_2(int): return int
    Returns n choose 2, or n(n-1)/2.
    """

    return (n * (n - 1)) / 2


def normalize(counts):
    """
    normalize(dict): None
    Normalizes the count dictionary
    """

    total = sum(counts.values())
    for label in counts:
        counts[label] /= total


def normalize_list(counts):
    """
    normalize(list): None
    Normalizes the count list
    """

    total = sum(counts)
    for i in range(len(counts)):
        counts[i] /= total

def lnormalize(counts):
    """
    lnormalize(dict): None
    Normalizes a log count dictionary in log space
    """

    lsum = float('-inf')
    for label in counts:
        lsum = ladd(lsum, counts[label])
    for label in counts:
        counts[label] -= lsum


def lnormalize_list(counts):
    lsum = float('-inf')
    for count in counts:
        lsum = ladd(lsum, count)
    for i in range(len(counts)):
        counts[i] -= lsum


def normalize_and_log(counts):
    """
    normalize_and_log(dict): None
    Normalizes and dictionary and then converts it to log space
    """

    total = sum(counts.values())
    for label in counts:
        if counts[label] == 0:
            counts[label] = float('-inf')
        else:
            counts[label] = math.log(counts[label] / total)


def normalize_and_log_list(counts):
    total = sum(counts)
    for i, x in enumerate(counts):
        if x == 0:
            counts[i] = float('-inf')
        else:
            counts[i] = math.log(x / total)


def top_n(counts, n):
    """
    top_n(list of int, int): return list of int
    Returns the indices of the top n counts in the given list of counts, 
    excluding any zero counts.
    """

    keys = [i for i in range(len(counts)) if counts[i] > 0]
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
