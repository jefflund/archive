from __future__ import division

from pytopic.util.sample import log_normalize

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

        log_normalize(self.label_counts)
        for label in self.labels:
            log_normalize(self.word_counts[label])

    def classify(self, data):
        return max(self.labels, key=lambda l: self._log_posterior(l, data))

    def _log_prior(self, label):
        return self.label_counts[label]

    def _log_likelihood(self, label, data):
        log_likelihood = 0
        for word in data:
            log_likelihood += self.word_counts[label][word]
        return log_likelihood

    def _log_posterior(self, label, data):
        return self._prior(label) + self._likelihood(label, data)

    def validate(self, labels, data):
        correct = 0
        for label, doc in zip(labels, data):
            if label == self.classify(doc):
                correct += 1
        return correct / len(data)
