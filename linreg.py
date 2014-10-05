import numpy
import random

import ga
from ga import mutation, crossover

def get_data(filename):
    lines = open(filename).readlines()
    lines = [line.strip().split(',') for line in lines]
    lines = [[float(x) for x in line] for line in lines]
    return [[1] + line[:-1] for line in lines], [line[-1:] for line in lines]


def loss(xs, ys, w):
    h = numpy.array(xs).dot(w)
    y = numpy.array(ys).flatten()
    return sum((y - h) ** 2)


def linreg_form(xs, ys):
    X, Y = numpy.matrix(xs), numpy.matrix(ys)
    w = (X.T * X).I * X.T * Y
    return numpy.array(w).flatten().tolist()


def linreg_grad(xs, ys, a=1e-3, delta=1e-10, iters=1000000000):
    w = [0 for _ in xs[0]]
    x, y = numpy.array(xs), numpy.array(ys).flatten()
    last = loss(xs, ys, w)

    for _ in xrange(iters):
        w = [wi + a * sum((y - x.dot(w)) * x[:,i]) for i, wi in enumerate(w)]
        yield w
        curr = loss(xs, ys, w)
        if abs(last - curr) < delta:
            break
        last = curr


def linreg_gene(xs, ys, pop_size=1000, iters=1000):
    k = len(xs[0])

    def init():
        return [random.gauss(0, 10) for _ in xrange(k)]

    def fitness(ind):
        return 1000000000 - loss(xs, ys, ind)

    linreg = ga.create_ga(init, fitness, cross=crossover.uniform,
                                         mutate=mutation.gauss)
    return linreg(pop_size, iters)
