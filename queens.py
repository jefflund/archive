#!/bin/env python

from __future__ import division

import random


def init_pop(n):
    return [[random.randrange(8) for _ in xrange(8)] for _ in xrange(n)]


def attacks(a, b):
    return a[0] == b[0] or a[1] == b[1] or abs(a[0]-b[0]) == abs(a[1]-b[1])


def fitness(state):
    score = 0
    for i in xrange(8):
        for j in xrange(i, 8):
            if not attacks((i, state[i]), (j, state[j])):
                score += 1
    return score


def evaluate_pop(pop):
    return {tuple(state): fitness(state) for state in pop}


def crossover(a, b):
    cut = random.randrange(8)
    return a[:cut] + b[cut:], b[:cut] + a[cut:]


def mutate(state, mut_rate):
    return [random.randrange(8) if random.random() < mut_rate else x for x in state]


def sample_pop(fit_pop):
    min_fit = min(fit_pop.itervalues())
    adjusted_pop = {state: fit-min_fit for state, fit in fit_pop.iteritems()}

    sample = random.uniform(0, sum(adjusted_pop.itervalues()))
    for state, score in adjusted_pop.iteritems():
        if sample <= score:
            return state
        sample -= score


def print_state(state):
    for y in xrange(8):
        row = []
        for x in xrange(8):
            ch = '*' if state[x] == y else ' '
            bg = 238 if x % 2 == y % 2 else 251
            row.append('\033[48;5;{};38;5;196m{}\033[0m'.format(bg, ch))
        print ''.join(row)


def main(pop_size, mut_rate, num_gens=float('inf')):
    pop = init_pop(pop_size)
    solutions = set()

    gen = 0
    while gen <= num_gens:
        gen += 1
        fit_pop = evaluate_pop(pop)

        best = max(fit_pop.itervalues())
        mean = sum(fit_pop.itervalues()) / len(fit_pop)
        print 'Gen:', gen, 'Max:', best, 'Mean:', mean, 'Found:', len(solutions)

        for state, fit in fit_pop.iteritems():
            if fit == 28 and state not in solutions:
                print_state(state)
                solutions.add(state)

        pop = [state for state, fit in fit_pop.iteritems() if fit == 28]
        while len(pop) < pop_size:
            a, b = sample_pop(fit_pop), sample_pop(fit_pop)
            a, b = crossover(a, b)
            a, b = mutate(a, mut_rate), mutate(b, mut_rate)
            pop += [a, b]

if __name__ == '__main__':
    main(1000, .01)
