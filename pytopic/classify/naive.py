from __future__ import division

from pytopic.util.sample import normalize

class NaiveBayes(object):

    def __init__(self, labels, data, prior=0):
        self.labels = set(labels)
        vocab = reduce(set.union, data, set())

        self.label_counts = {l: 0 for l in self.labels}
        self.word_counts = {l: {w: prior for w in vocab} for l in self.labels}

        for label, doc in zip(labels, data):
            self.label_counts[label] += 1
            for word in doc:
                self.word_counts[label][word] += 1

        normalize(self.label_counts)
        for label in self.labels:
            normalize(self.word_counts[label])

    def classify(self, data):
        return max(self.labels, key=lambda label: self._posterior(label, data))

    def _prior(self, label):
        return self.label_counts[label]

    def _likelihood(self, label, data):
        likelihood = 1
        for word in data:
            likelihood *= self.word_counts[label][word]
        return likelihood

    def _posterior(self, label, data):
        return self._prior(label) * self._likelihood(label, data)
