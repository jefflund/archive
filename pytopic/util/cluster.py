"""Functionality for evaluating clustering quality"""

from __future__ import division

import os
import math
from pytopic.util.compute import n_choose_2

class Clustering(object):
    """Abstraction for clusterings, both labeled data and inferred clusters"""

    def __init__(self, labels, data):
        self.labels = labels
        self.data = data

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
        return Clustering(set(data), data)

    def __len__(self):
        return len(self.data)

    def __getitem__(self, index):
        return self.data[index]


class Contingency(object):
    """Represents a contingency matrix for two Clusterings on the same data"""

    def __init__(self, gold, pred):
        assert len(gold) == len(pred)

        self.gold = list(gold.labels)
        self.pred = list(pred.labels)

        self.counts = {(g, p): 0 for g in self.gold for p in self.pred}
        for index in zip(gold.data, pred.data):
            self.counts[index] += 1

    def __len__(self):
        return sum(self.counts.values())

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

def f_measure(contingency):
    class_counts = {c: sum(contingency[c, k] for k in contingency.pred)
                    for c in contingency.gold}
    clust_counts = {k: sum(contingency[c, k] for c in contingency.gold)
                    for k in contingency.pred}

    f_measures = {}
    for c in contingency.gold:
        for k in contingency.pred:
            recall = contingency[c, k] / class_counts[c]
            precision = contingency[c, k] / clust_counts[k]
            try:
                f_measures[c, k] = recall * precision / (recall + precision)
            except ZeroDivisionError:
                f_measures[c, k] = 0

    result = 0
    normalizer = len(contingency)
    for c in contingency.gold:
        best_f_measure = max(f_measures[c, k] for k in contingency.pred)
        result += best_f_measure * class_counts[c] / normalizer
    return 2 * result

def ari(contingency):
    a_part = sum(n_choose_2(sum(contingency[c, k] for k in contingency.pred))
                 for c in contingency.gold)
    b_part = sum(n_choose_2(sum(contingency[c, k] for c in contingency.gold))
                 for k in contingency.pred)
    index = sum(n_choose_2(contingency[c, k])
            for c in contingency.gold
            for k in contingency.pred)
    expected = (a_part * b_part) / n_choose_2(len(contingency))
    maximum = (a_part + b_part) / 2
    return (index - expected) / (maximum - expected)

def variation_info(contingency):
    num_datums = len(contingency)
    gold_sums = {c:sum(contingency[c, k] for k in contingency.pred)
                 for c in contingency.gold}
    pred_sums = {k: sum(contingency[c, k] for c in contingency.gold)
                 for k in contingency.pred}

    entropy_gold = 0
    for c in contingency.gold:
        prob_c = gold_sums[c] / num_datums
        entropy_gold -= prob_c * math.log(prob_c)

    entropy_pred = 0
    for k in contingency.pred:
        prob_k = pred_sums[k] / num_datums
        entropy_pred -= prob_k * math.log(prob_k)

    mutal_information = 0
    for c in contingency.gold:
        for k in contingency.pred:
            prob_ck = contingency[c, k] / num_datums
            prob_c = gold_sums[c] / num_datums
            prob_k = pred_sums[k] / num_datums
            cond_prob = prob_ck / (prob_c * prob_k)
            if cond_prob != 0 and not math.isnan(cond_prob):
                mutal_information += prob_ck * math.log(cond_prob)

    return entropy_gold + entropy_pred - 2 * mutal_information
