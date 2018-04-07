"""Provides functions for selection in a genetic algorithm"""

import random
import functools


def fitness_proportionate(population):
    """Performs fitness proportionate selection on a dict mapping individuals
    to fitness values."""

    sample = random.uniform(0, sum(population.itervalues()))
    for individual, fitness in population.iteritems():
        if sample <= fitness:
            return individual
        sample -= fitness


def tournament(k, p, population):
    """Performs tournament selection with size k and probability p."""
    entrants = random.sample(population, k)
    entrant = None
    for i, entrant in enumerate(entrants):
        if random.random() < p * (1-p) ** i:
            break
    return entrant


def create_tournament(k, p=1):
    """Creates a tournament selection with size k and probability p."""
    return functools.partial(tournament, k, p)
