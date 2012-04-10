"""Functionality for evaluating clustering quality"""

import os

class Clustering(object):
    """Abstraction for clusterings, both labeled data and inferred clusters"""

    def __init__(self, labels, data):
        self.labels = set(labels)
        self.data = data

    def __len__(self):
        return len(self.data)

    def __getitem__(self, index):
        return self.data[index]

    @classmethod
    def from_model(cls, model):
        """
        Clustering.from_model(TopicModel): return Clustering
        Returns a Clustering using the inferred clusters from a TopicModel.
        That TopicModel must have the attributes K and k, indicating the
        number of clusters and the cluster assignments for each document.
        """

        return Clustering(range(model.K), model.k)

    @classmethod
    def from_corpus(cls, corpus):
        """
        Clustering.from_corpus(Corpus): return Clustering
        Returns a Clustering using the folder structure of the document titles
        to determine the labels.
        """

        data = [os.path.dirname(title) for title in corpus.titles]
        return Clustering(data, data)


class Contingency(object):
    """Represents a contingency matrix for two Clusterings on the same data"""

    def __init__(self, gold, pred):
        assert len(gold) == len(pred)

        self.gold = gold.labels
        self.pred = pred.labels

        try:
            self.counts = {(g, p): 0 for g in self.gold for p in self.pred}
            for index in zip(gold.data, pred.data):
                self.counts[index] += 1
        except KeyError as err:
            print self.counts
            raise err

    def __len__(self):
        return len(self.counts)

    def __getitem__(self, index):
        return self.counts[index]

    def print_contingency(self):
        """
        Contingency.print_contingency(): return None
        Pretty prints the contingency matrix to stdout
        """

        gold_size = max(len(str(label)) for label in self.gold)
        pred_size = max(len(str(label)) for label in self.pred)

        print ' ' * gold_size,
        for pred_label in self.pred:
            print str(pred_label).ljust(pred_size),
        print

        for gold_label in self.gold:
            print str(gold_label).ljust(gold_size),
            for pred_label in self.pred:
                print str(self[gold_label, pred_label]).ljust(pred_size),
            print
