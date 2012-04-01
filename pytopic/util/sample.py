"""Utilities for sampling from various distributions"""

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

    raise ValueError()

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
    neg_diff = log_y - log_x
    if neg_diff < -50:
        return log_x
    return log_x + math.log(1 + math.exp(neg_diff))
