"""Provides functions for mutation in a genetic algorithm"""

import random
import functools


def _flip(mutate_chance):
    return random.random() < mutate_chance


def simple(ind, mutate_chance, init):
    """Replaces random values with ones from a randomly initialized gene"""
    return [m if _flip(mutate_chance) else x for x, m in zip(ind, init())]


def create_simple(init):
    """Creates a simple mutation function from an initialization function"""
    return functools.partial(simple, init=init)


def gauss(ind, mutate_chance, sigma=1):
    """Perturbs mutated values with Gaussian noise"""
    return [random.gauss(x, sigma) if _flip(mutate_chance) else x for x in ind]


def create_gauss(sigma):
    """Creates a guassian mutation with a custom value for sigma"""
    return functools.partial(gauss, sigma=sigma)
