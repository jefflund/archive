#!/bin/env python

from __future__ import division

import random

import ga

def queens_init():
    return [random.randrange(8) for _ in xrange(8)]


def queens_fitness(ind):
    score = 0
    for i in xrange(8):
        for j in xrange(i, 8):
            if ind[j] != ind[i] and abs(ind[j]-ind[i]) != j-i:
                score += 1
    return score


def queen_goal(_, fitness):
    return fitness == 28


if __name__ == '__main__':
    queens = ga.create_ga(queens_init, queens_fitness, goal=queen_goal)
    solution = queens(1000, 100)

    print solution, queens_fitness(solution)
    for y in xrange(8):
        row = []
        for x in xrange(8):
            ch = '*' if solution[x] == y else ' '
            bg = 238 if x % 2 == y % 2 else 251
            row.append('\033[48;5;{};38;5;196m{}\033[0m'.format(bg, ch))
        print ''.join(row)
