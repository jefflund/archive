from __future__ import division

import os
import math

class NaiveBayes(object):

    def __init__(self, labels, data, prior=0):
        label_set = set(labels)
        word_set = reduce(set.union, data, set())

        self.label_counts = {l: prior for l in label_set}
        for label in labels:
            self.label_counts[label] += 1
        norm = sum(self.label_counts.values())
        for label in label_set:
            self.label_counts[label] /= norm
            self.label_counts[label] = math.log(self.label_counts[label])

        self.word_counts = {l: {w: prior for w in word_set} for l in label_set}
        for label, doc in zip(labels, data):
            for word in doc:
                self.word_counts[label][word] += 1
        for label in label_set:
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

    def classify(self, data):
        best_posterior = float('-inf')
        best_label = None

        for label in self.label_counts:
            posterior = self._log_posterior(label, data)
            if posterior > best_posterior:
                best_posterior = posterior
                best_label = label

        return best_label

    def validate(self, labels, data):
        correct = 0

        for label, doc in zip(labels, data):
            if label == self.classify(doc):
                correct += 1

        return correct / len(data)

def cross_fold_validate(labels, data, n=10):
    accuracies = []

    val_size = len(data) // n
    for i in range(n):
        start, end = i * val_size, (i + 1) * val_size
        train_labels = labels[:start] + labels[end:]
        train_data = data[:start] + data[end:]
        val_labels = labels[start: end]
        val_data = data[start: end]

        classifier = NaiveBayes(train_labels, train_data)
        accuracies.append(classifier.validate(val_labels, val_data))

    return sum(accuracies) / len(accuracies)

def validate_model(model, n=10):
    labels = [os.path.dirname(title) for title in model.titles]
    return cross_fold_validate(labels, model.z, n)
