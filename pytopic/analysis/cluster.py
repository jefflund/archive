"""Functionality for evaluating clustering quality"""

from __future__ import division

import os
import math
from pytopic.util.compute import n_choose_2, lim_plogp, lim_xlogy

class Clustering(object):
    """Abstraction for clusterings, both labeled data and inferred clusters"""

    def __init__(self, labels, data):
        self.labels = labels
        self.data = data

    @classmethod
    def from_model(cls, model):
        """
        Clustering.from_model(TopicModel): Clustering
        Returns a Clustering using the inferred clusters from a TopicModel.
        That TopicModel must have the attributes K and k, indicating the
        number of clusters and the cluster assignments for each document.
        """

        return Clustering(range(model.K), model.k)

    @classmethod
    def from_corpus(cls, corpus):
        """
        Clustering.from_corpus(Corpus): Clustering
        Returns a Clustering using the folder structure of the document titles
        to determine the labels.
        """

        data = [os.path.dirname(title) for title in corpus.titles]
        return Clustering(set(data), data)

    @classmethod
    def from_indices(cls, corpus, dirpath, data_dir=''):
        """
        Clustering.from_indices(Corpus, str, str): Clustering
        Returns a Clustering using the index structure. The provided corpus is
        used to order the clustering data. Each index file is assumed to
        represent a separate clustering
        """

        labels = set()
        data = {}

        for root, _, files in os.walk(dirpath):
            for indexfile in files:
                indexfile = os.path.join(root, indexfile)
                labels.add(indexfile)
                with open(indexfile) as filelist:
                    for filename in filelist:
                        filename = filename.strip()
                        if len(filename) > 0:
                            filename = os.path.join(data_dir, filename)
                            data[filename] = indexfile

        data = [data[corpus.titles[d]] for d in range(len(corpus))]
        return Clustering(labels, data)

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
        Contingency.print_contingency(): None
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
    """
    f_measure(Contingency): float
    Computes the F-1 measure for the given contingency matrix
    """

    class_counts = {c: sum(contingency[c, k] for k in contingency.pred)
                    for c in contingency.gold}
    clust_counts = {k: sum(contingency[c, k] for c in contingency.gold)
                    for k in contingency.pred}

    f_measures = {}
    for c in contingency.gold:
        for k in contingency.pred:
            try:
                recall = contingency[c, k] / class_counts[c]
                precision = contingency[c, k] / clust_counts[k]
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
    """
    ari(Contingency): float
    Computes the average rand index for the given contingency matrix
    """

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
    """
    variation_info(Contingency): float
    Computes the variation of information for the given contingency matrix
    """

    num_datums = len(contingency)
    gold_sums = {c:sum(contingency[c, k] for k in contingency.pred)
                 for c in contingency.gold}
    pred_sums = {k: sum(contingency[c, k] for c in contingency.gold)
                 for k in contingency.pred}

    entropy_gold = 0
    for c in contingency.gold:
        prob_c = gold_sums[c] / num_datums
        entropy_gold -= lim_plogp(prob_c)

    entropy_pred = 0
    for k in contingency.pred:
        prob_k = pred_sums[k] / num_datums
        entropy_pred -= lim_plogp(prob_k)

    mutual_information = 0
    for c in contingency.gold:
        for k in contingency.pred:
            prob_ck = contingency[c, k] / num_datums
            prob_c = gold_sums[c] / num_datums
            prob_k = pred_sums[k] / num_datums
            if prob_c != 0 and prob_k != 0:
                mutual_prob = prob_ck / (prob_c * prob_k)
                mutual_information += lim_xlogy(prob_ck, mutual_prob)

    return entropy_gold + entropy_pred - 2 * mutual_information
