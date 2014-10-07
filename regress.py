#!/bin/env python

from __future__ import division

import math
import sys
import random
import functools


class Node(object):

    def __init__(self, bias, weights):
        self.bias = bias
        self.weights = weights

    def activate(self, inputs):
        x = self.bias + sum(w * i for w, i in zip(self.weights, inputs))
        return 1 / (1 + math.exp(-x))

    @staticmethod
    def init(num_inputs):
        return Node([random.gauss(0, 1) for _ in xrange(num_inputs)])

    def mutate(self, mutate_chance):
        if random.random() < mutate_chance:
            self.bias = random.gauss(self.bias, 1)

        for i, w in enumerate(self.weights):
            if random.random() < mutate_chance:
                self.weights[i] = random.gauss(w, 1)


class Layer(object):

    def __init__(self, nodes):
        self.nodes = nodes

    def activate(self, inputs):
        return [node.activate(inputs) for node in self.nodes]

    @staticmethod
    def init(num_inputs, num_nodes):
        return Layer([Node.init(num_inputs) for _ in xrange(num_nodes)])

    def mutate(self, mutate_chance):
        for node in self.nodes:
            node.mutate(mutate_chance)


class Network(object):

    def __init__(self, layers):
        self.layers = layers

    def activate(self, inputs):
        return reduce(lambda i, n: n.activate(i), self.layers, inputs)

    @staticmethod
    def init(num_inputs, num_hidden):
        hidden = Layer.init(num_inputs, num_hidden)
        output = Node.init(num_hidden)
        return Network([hidden, output])

    def mutate(self, mutate_chance):
        for layer in self.layers:
            layer.mutate(mutate_chance)


def get_data(filename):
    lines = open(filename).readlines()
    lines = [line.strip().split(',') for line in lines]
    lines = [[float(x) for x in line] for line in lines]
    return [line[:-1] for line in lines], [line[-1] for line in lines]
