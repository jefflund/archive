from __future__ import division

from pytopic.util.sample import log_normalize

class NaiveBayes(object):

    def __init__(self, labels, data):
        print data

        self.labels = set(labels)
        vocab = reduce(set.union, data, set())

        self.label_counts = {l: 0 for l in self.labels}
        self.word_counts = {l: {w: 0 for w in vocab} for l in self.labels}

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
        return self._log_prior(label) + self._log_likelihood(label, data)

    def validate(self, labels, data):
        correct = 0
        for label, doc in zip(labels, data):
            predicted = self.classify(doc)
            if label == predicted:
                correct += 1
            print label, data, '=>', predicted
        return correct / len(data)

def partition(labels, data, n):
    data = [data[i::n] for i in range(n)]
    labels = [labels[i::n] for i in range(n)]

    for i in range(n):
        train_data = sum(data[:i] + data[i + 1:], [])
        train_labels = sum(labels[:i] + labels[i + 1:], [])
        yield (train_labels, train_data), (labels[i], data[i])

def cross_fold_validation(labels, data, n=10):
    accuracy = 0
    for train, test in partition(labels, data, n):
        train_labels, train_data = train
        classifier = NaiveBayes(train_labels, train_data)

        test_labels, test_data = test
        accuracy += classifier.validate(test_labels, test_data)
    return accuracy / n
