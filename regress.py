#!/bin/env python

from __future__ import division

import numpy
import math
import sys
import random
import functools

import ga
from ga import crossover


class Node(object):

    def __init__(self, bias, weights):
        self.bias = bias
        self.weights = weights

    def activate(self, inputs):
        x = self.bias + sum(w * i for w, i in zip(self.weights, inputs))
        return 1 / (1 + math.exp(-x))

    @staticmethod
    def init(num_inputs):
        bias = random.gauss(0, 1)
        weights = [random.gauss(0, 1) for _ in xrange(num_inputs)]
        return Node(bias, weights)

    @staticmethod
    def copy(node):
            return Node(node.bias, list(node.weights))

    def cross(self, other):
        a, b = crossover.one_point(self.weights, other.weights)
        return Node(self.bias, a), Node(other.bias, b)

    def mutate(self, mutate_chance):
        if random.random() < mutate_chance:
            self.bias = random.gauss(self.bias, 1)

        for i, weight in enumerate(self.weights):
            if random.random() < mutate_chance:
                self.weights[i] = random.gauss(weight, 1)

        return self


class Layer(object):

    def __init__(self, nodes):
        self.nodes = nodes

    def activate(self, inputs):
        return [node.activate(inputs) for node in self.nodes]

    @staticmethod
    def init(num_inputs, num_nodes):
        return Layer([Node.init(num_inputs) for _ in xrange(num_nodes)])

    def cross(self, other):
        a, b = crossover.one_point(self.nodes, other.nodes)
        a, b = [Node.copy(n) for n in a], [Node.copy(n) for n in b]
        return Layer(a), Layer(b)

    def mutate(self, mutate_chance):
        for node in self.nodes:
            node.mutate(mutate_chance)

        return self


class Network(object):

    def __init__(self, layers):
        self.layers = layers

    def activate(self, inputs):
        return reduce(lambda i, n: n.activate(i), self.layers, inputs)

    def sse(self, xs, ys):
        loss = 0
        for x, y in zip(xs, ys):
            loss += (y - self.activate(x)) ** 2
        return loss

    @staticmethod
    def init(num_inputs, num_hidden):
        hidden = Layer.init(num_inputs, num_hidden)
        output = Node.init(num_hidden)
        return Network([hidden, output])

    def cross(self, other):
        layers = [a.cross(b) for a, b in zip(self.layers, other.layers)]
        a, b = zip(*layers)
        return Network(list(a)), Network(list(b))

    def mutate(self, mutate_chance):
        for layer in self.layers:
            layer.mutate(mutate_chance)

        return self


def get_data(filename):
    lines = open(filename).readlines()
    lines = [line.strip().split(',') for line in lines]
    lines = [[float(x) for x in line] for line in lines]
    return [line[:-1] for line in lines], [line[-1] for line in lines]


def linear_regress(xs, ys):
    X = numpy.matrix([[1] + x for x in xs])
    Y = numpy.matrix([[y] for y in ys])
    return (X.T * X).I * X.T * Y


def linear_sse(xs, ys, w=None):
    if w is None:
        w = linear_regress(xs, ys)
    h = numpy.array([[1] + x for x in xs]).dot(w)
    y = numpy.array([[y] for y in ys])
    return (numpy.array(y-h) ** 2).sum()


if __name__ == '__main__':
    xs, ys = get_data('nonlinear_data-train.csv')

    w = linear_regress(xs, ys)
    print 'linear train:', linear_sse(xs, ys, w)

    init = functools.partial(Network.init, len(xs[0]), 10)
    fitness = lambda n: 1e6 - n.sse(xs, ys)
    cross = lambda a, b: a.cross(b)
    mutate = lambda n, p: n.mutate(p)

    regress = ga.create_ga(init, fitness, cross=cross, mutate=mutate)
    solution = regress(100, 100)
    print 'non-linear train:', solution.sse(xs, ys)

    xs, ys = get_data('nonlinear_data-test.csv')

    print 'linear test:', linear_sse(xs, ys, w)
    print 'non-linear test:', solution.sse(xs, ys)
