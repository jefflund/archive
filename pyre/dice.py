"""Provides functions for dealing with random numbers in a roguelike"""

import random
import math

from pyre import types


def chance(probability):
    """Returns True with the given probability"""
    return random.random() < probability


def coinflip():
    """Returns True with probability 0.5"""
    return random.getrandbits(1) == 0


def rand_coordinate(startx, starty, stopx=None, stopy=None):
    """Returns a random coordinate within the given bounds"""
    return types.Coordinate(random.randrange(startx, stopx),
                            random.randrange(starty, stopy))


def rand_direction(include_origin=False):
    """Returns a random cardinal direction as a Coordinate"""
    x, y = random.randint(-1, 1), random.randint(-1, 1)
    if not include_origin:
        while x == 0 and y == 0:
            x, y = random.randint(-1, 1), random.randint(-1, 1)
    return types.Coordinate(x, y)


def roll_xdy(xdy):
    """Returns the result of x y-sided dice rolls"""
    return sum(random.randint(1, xdy[1]) for _ in xrange(xdy[0]))


def roll_dy(y):
    """Returns the result of a y-sided dice roll"""
    return random.randint(1, y)


class RollResult(int):
    """An integer which evaluates True only if it is greater than zero"""

    def __new__(cls, value):
        return int.__new__(cls, value)

    def __nonzero__(self):
        return self > 0


def _opposed_roll(skill, rating, y):
    result = RollResult(skill + roll_dy(y) - rating - roll_dy(y))
    winner = skill if result else rating
    winner.train()
    return result


def combat_roll(skill, rating):
    """Returns the results of an opposed combat roll, and trains the winner"""
    return _opposed_roll(skill, rating, 20)


def skill_roll(skill, rating):
    """Returns the results of an opposed skill roll, and trains the winner"""
    return _opposed_roll(skill, rating, 10)


def damage_roll(damage, armor):
    """Returns the result of an opposed xdy roll, with a minimum of 0"""
    return max(roll_xdy(damage) - sum(roll_xdy(piece) for piece in armor), 0)


def sample_poisson(rate):
    """Returns a sample from a Poisson distribution with the given rate"""

    invrate = math.exp(-rate)
    product = random.random()
    sample = 0
    while product > invrate:
        sample += 1
        product *= random.random()
    return sample


class Categorical(object):
    """An emperical based categorical distribution"""

    def __init__(self, support, prior):
        self.counts = {x: prior for x in support}
        self.total = len(support) * prior

    def observe(self, event, count=1):
        """Updates the model with an observation"""
        self.counts[event] += count
        self.total += count

    def sample(self):
        """Samples an event from the distribution"""
        sample = random.uniform(0, self.total)
        for event, count in self.counts.iteritems():
            if sample <= count:
                return event
            sample -= count


class MarkovModel(object):
    """A Markov model with a Dirichlet prior and simplified Katz backoff"""

    def __init__(self, support, order, prior, boundary=None):
        self.support = set(support)
        self.support.add(boundary)
        self.order = order
        self.prior = prior
        self.boundary = boundary
        self.prefix = [boundary] * self.order
        self.postfix = [boundary]
        self.counts = {}

    def _categorical(self, context):
        if context not in self.counts:
            self.counts[context] = Categorical(self.support, self.prior)
        return self.counts[context]

    def _backoff(self, context):
        context = tuple(context)
        if len(context) > self.order:
            context = context[-self.order:]
        elif len(context) < self.order:
            context = (self.boundary,) * (self.order - len(context)) + context

        while context not in self.counts and len(context) > 0:
            context = context[1:]
        return context

    def observe(self, sequence, count=1):
        """Adds a training example to the model"""
        sequence = self.prefix + list(sequence) + self.postfix
        for i in range(self.order, len(sequence)):
            context = tuple(sequence[i - self.order:i])
            event = sequence[i]
            for j in range(len(context) + 1):
                self._categorical(context[j:]).observe(event, count)

    def sample(self, context):
        """Generates a sample from the model given a particular context"""
        context = self._backoff(context)
        return self._categorical(context).sample()

    def generate(self):
        """Generates a hypothetical sequence from the model"""
        sequence = [self.sample(self.prefix)]
        while sequence[-1] != self.boundary:
            sequence.append(self.sample(sequence))
        return sequence[:-1]


# These are for convenience, so only dice need be imported for randomness
# pylint: disable=C0103
choice = random.choice
randrange = random.randrange
