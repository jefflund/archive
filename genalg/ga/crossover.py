"""Provides functions for crossover in a genetic algorithm"""

import random


def one_point(ind_a, ind_b):
    """Performs one-point crossover"""
    cut = random.randrange(1, min(len(ind_a), len(ind_b))-1)
    return ind_a[:cut] + ind_b[cut:], ind_b[:cut] + ind_a[cut:]


def two_point(ind_a, ind_b):
    """Performs two-point crossover"""
    cut1 = random.randrange(1, min(len(ind_a), len(ind_b))-1)
    cut2 = random.randrange(1, min(len(ind_a), len(ind_b))-1)
    return (ind_a[:cut1] + ind_b[cut1:cut2]+ ind_a[cut2:],
            ind_b[:cut1] + ind_a[cut1:cut2]+ ind_b[cut2:])


def uniform(ind_a, ind_b):
    """Performs uniform crossover"""
    flips = [random.random() < .5 for _ in xrange(len(ind_a))]
    return (tuple(a if flip else b for a, b, flip in zip(ind_a, ind_b, flips)),
            tuple(b if flip else a for a, b, flip in zip(ind_a, ind_b, flips)))
