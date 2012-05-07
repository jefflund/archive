"""Test of the various clustering metrics"""

import unittest
from pytopic.util.cluster import Clustering, Contingency, f_measure, ari, \
    variation_info
from pytopic.pipeline.corpus import CorpusReader
from pytopic.topic.mixmulti import MixtureMultinomial

class TestContingency(unittest.TestCase):

    def test_clustering(self):
        self.gold_labels = set(['a', 'b', 'c'])
        self.pred_labels = set([1, 2, 3])
        self.gold_data = ['a', 'a', 'a', 'a', 'b', 'b', 'b', 'c', 'c', 'c']
        self.pred_data = [1, 1, 1, 2, 2, 2, 2, 3, 3, 2]

        gold = Clustering(self.gold_labels, self.gold_data)
        self.assertEqual(self.gold_labels, gold.labels)
        self.assertEqual(self.gold_data, gold.data)

    def test_corpus_clustering(self):
        reader = CorpusReader()
        reader.add_dir('test_data/lorum')
        reader.add_dir('test_data/ipsum')
        corpus = reader.read()
        gold = Clustering.from_corpus(corpus)

        self.assertSetEqual(set(['test_data/lorum', 'test_data/ipsum']),
                            gold.labels)

        for d in range(len(corpus)):
            title = corpus.titles[d]
            label = gold.data[d]
            self.assertTrue(title.startswith(label))

    def test_model_clustering(self):
        reader = CorpusReader()
        reader.add_dir('test_data/lorum')
        corpus = reader.read()
        model = MixtureMultinomial(corpus, 4, 2, .01)
        gold = Clustering.from_model(model)

        self.assertSetEqual(set(range(4)), gold.labels)

        for d in range(model.M):
            self.assertEqual(model.k[d], gold.data[d])


class TestMetrics(unittest.TestCase):

    def setUp(self):
        labels = [1, 2, 3]
        gold_data = [1, 1, 2, 2, 2, 2, 3, 3, 3, 3]
        pred_data = [1, 2, 1, 2, 2, 3, 3, 3, 3, 3]
        gold = Clustering(labels, gold_data)
        pred = Clustering(labels, pred_data)
        self.contingency = Contingency(gold, pred)

    def test_f_measure(self):
        self.assertAlmostEqual(0.684, f_measure(self.contingency), 3)

    def test_ari(self):
        self.assertAlmostEqual(0.313, ari(self.contingency), 3)

    def test_vi(self):
        self.assertAlmostEqual(1.134, variation_info(self.contingency), 3)
