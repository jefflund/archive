from __future__ import division

import math

class NaiveBayes(object):

    def __init__(self, labels, data, smooth=1):
        label_set = set(labels)
        word_set = reduce(set.union, data, set())

        self.label_counts = {l: 0 for l in label_set}
        for label in labels:
            self.label_counts[label] += 1
        norm = sum(self.label_counts.values())
        for label in label_set:
            self.label_counts[label] /= norm
            self.label_counts[label] = math.log(self.label_counts[label])

        self.word_counts = {l: {w: 0 for w in word_set} for l in label_set}
        for label, doc in zip(labels, data):
            for word in doc:
                self.word_counts[label][word] += 1
        for label in labels:
            counter = self.word_counts[label]
            norm = sum(counter.values())
            for word in word_set:
                counter[word] /= norm
                if counter[word] == 0:
                    counter[word] = float('-inf')
                else:
                    counter[word] = math.log(counter[word])

    def _log_prior(self, label):
        return self.label_counts[label]

    def _log_likelihood(self, label, data):
        likelihood = 0
        for word in data:
            likelihood += self.word_counts[label][word]
        return likelihood

    def _log_posterior(self, label, data):
        return self._log_likelihood(label, data) + self._log_prior(label)
