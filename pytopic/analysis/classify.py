from __future__ import division

def normalize_and_log(counts):
    """
    normalize_and_log(dict): None
    Normalizes and dictionary and then converts it to log space
    """

    total = sum(counts.values())
    for label in counts:
        if counts[label] == 0:
            counts[label] = float('-inf')
        else:
            counts[label] = math.log(counts[label] / total)


class NaiveBayes(object):
    """A simple Multinomial Naive Bayes classifier"""

    def __init__(self, labels, data):
        self.labels = set(labels)
        vocab = reduce(set.union, data, set())

        self.label_counts = {l: 0 for l in self.labels}
        self.word_counts = {l: {w: 0 for w in vocab} for l in self.labels}

        for label, doc in zip(labels, data):
            self.label_counts[label] += 1
            for word in doc:
                self.word_counts[label][word] += 1

        normalize_and_log(self.label_counts)
        for label in self.labels:
            normalize_and_log(self.word_counts[label])

    def classify(self, data):
        """
        NaiveBayes.classify(iterable): object
        Returns the label with the maximum posterior probability given the data
        """

        return max(self.labels, key=lambda l: self._log_posterior(l, data))

    def _log_prior(self, label):
        return self.label_counts[label]

    def _log_likelihood(self, label, data):
        log_likelihood = 0
        for word in data:
            # get defaults to 0 so we ignore totally unobserved words
            log_likelihood += self.word_counts[label].get(word, 0)
        return log_likelihood

    def _log_posterior(self, label, data):
        return self._log_prior(label) + self._log_likelihood(label, data)

    def validate(self, labels, data):
        """
        NaiveBayes.validate(list of object, list of iterable): float
        Computes the accuracy of the model on a list of data, using the given
        labels as the true labels
        """

        correct = 0
        for label, doc in zip(labels, data):
            predicted = self.classify(doc)
            if label == predicted:
                correct += 1
        return correct / len(data)


def partition(labels, data, n):
    """
    partition(list of object, list of iterable, int): yield tuple of iterable
    Splits the data and labels into n partitions, and then yields n different 
    training and test sets. Each consists of a pair of labels and data.
    """

    labels = [labels[i::n] for i in range(n)]
    data = [data[i::n] for i in range(n)]

    for i in range(n):
        train_labels = sum(labels[:i] + labels[i + 1:], [])
        train_data = sum(data[:i] + data[i + 1:], [])
        yield (train_labels, train_data), (labels[i], data[i])


def cross_fold_validation(labels, data, n=10):
    """
    cross_fold_validation(list of object, list of iterable, int): float
    After using partition to split the data n ways, trains up n models on the
    partition training data and averages the accuracy on the test sets. This is
    a way to validate the features in the data.
    """

    accuracy = 0
    for train, test in partition(labels, data, n):
        train_labels, train_data = train
        classifier = NaiveBayes(train_labels, train_data)

        test_labels, test_data = test
        accuracy += classifier.validate(test_labels, test_data)
    return accuracy / n
