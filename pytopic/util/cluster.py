"""Functionality for evaluating clustering quality"""

import os

class Clustering(object):

    def __init__(self, labels, data):
        self.labels = labels
        self.data = data

    def __len__(self):
        return len(self.data)

    def __getitem__(self, index):
        return self.data[index]

    @classmethod
    def from_model(cls, model):
        return Clustering(range(model.K), model.k)

    @classmethod
    def from_corpus(cls, corpus):
        data = [os.path.dirname(title) for title in corpus.titles]
        return Clustering(set(data), data)


class Contingency(object):

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
        gold_size = max(len(str(label)) for label in self.gold)
        pred_size = max(len(str(label)) for label in self.pred)

        print ' ' * gold_size,
        for pred_label in self.pred:
            print _padded_str(pred_label, pred_size),
        print

        for gold_label in self.gold:
            print _padded_str(gold_label, gold_size),
            for pred_label in self.pred:
                print _padded_str(self[gold_label, pred_label], pred_size),
            print


def _padded_str(item, length):
    item = str(item)
    return item + ' ' * (len(item) - length)



