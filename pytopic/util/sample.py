"""Utilities for sampling from various distributions"""

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

def sample_order(dim):
    """
    sample_order(int): return list of int
    Returns the range up to the given dimension, shuffled for sampling
    """

    order = range(dim)
    random.shuffle(order)
    return order

