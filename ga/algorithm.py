"""Provides functions for creating genetic algorithms"""

from . import selection, crossover, mutation

def create_ga(init, fitness, select=selection.fitness_proportionate,
                             cross=crossover.one_point,
                             mutate=None,
                             goal=None):
    """Creates a customized genetic algorithm"""
    if mutate is None:
        mutate = mutation.create_simple(init)

    def run_ga(pop_size, max_gens, eliteness=.1, mutate_chance=.01):
        """Runs a customized genetic algorithm"""
        pop = [init() for _ in xrange(pop_size)]
        elite_size = int(pop_size * eliteness)

        for _ in xrange(max_gens):
            pop_fit = {ind: fitness(ind) for ind in pop}

            if goal and any(goal(ind, fit) for ind, fit in pop_fit.iteritems()):
                break

            if elite_size > 0:
                pop = sorted(pop_fit, key=pop_fit.get, reverse=True)
                pop = pop[:elite_size]
            else:
                pop = []

            while len(pop) < pop_size:
                ind_a, ind_b = select(pop_fit), select(pop_fit)
                ind_a, ind_b = cross(ind_a, ind_b)
                ind_a = mutate(ind_a, mutate_chance)
                ind_b = mutate(ind_b, mutate_chance)
                pop += [ind_a, ind_b]

        return max(pop, key=fitness)

    return run_ga
