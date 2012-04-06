"""Test of the various clustering metrics"""

import unittest
from util.cluster import Clustering

class TestContingency(unittest.TestCase):

    def setUp(self):
        self.gold_labels = ['a', 'b', 'c']
        self.pred_labels = [1, 2, 3]
        self.gold_data = ['a', 'a', 'a', 'a', 'b', 'b', 'b', 'c', 'c', 'c']
        self.pred_data = [1, 1, 1, 2, 2, 2, 2, 3, 3, 2]

    def test_clustering(self):
        gold = Clustering(self.gold_labels, self.gold_data)
        self.assertEqual(self.gold_labels, gold.labels)
        self.assertEqual(self.gold_data, gold.data)

