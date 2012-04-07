"""Ensures that topic models do not crash - results to be verified by hand"""

import unittest
from pipeline.corpus import CorpusReader, Tokenizer
from util.data import load_stopwords
from topic.model import TopicModel
from topic.vanilla import VanillaLDA
from topic.cluster import ClusterLDA
from topic.mixmulti import MixtureMultinomial

class TestModel(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        reader = CorpusReader(Tokenizer())
        reader.add_dir('test_data/lorum')
        cls.corpus = reader.read()

    def setUp(self):
        self.corpus = TestModel.corpus
        self.model = TopicModel(self.corpus)

    def test_model(self):
        self.assertSequenceEqual(self.corpus.titles, self.model.titles)
        self.assertSequenceEqual(self.corpus.vocab, self.model.vocab)
        self.assertSequenceEqual(self.corpus.data, self.model.w)
        self.assertEqual(len(self.corpus), self.model.M)
        for d in range(len(self.corpus)):
            self.assertEqual(len(self.corpus.data[d]), self.model.N[d])
        self.assertEqual(len(self.corpus.vocab), self.model.V)

    def test_inference(self):
        i = [0]
        def inc():
            i[0] += 1
        self.model.sample = inc
        self.model.inference(1000)
        self.assertEqual(1000, i[0])


class TestLDA(TestModel):

    def setUp(self):
        self.corpus = TestLDA.corpus
        self.T = 10
        self.alpha = .5
        self.beta = .01
        self.model = VanillaLDA(self.corpus, self.T, self.alpha, self.beta)

    def test_model(self):
        TestModel.test_model(self)

        self.assertEqual(self.T, self.model.T)
        self.assertEqual(self.alpha, self.model.alpha)
        self.assertEqual(self.beta, self.model.beta)

        self.assertEqual(self.model.M, len(self.model.z))
        for d in range(self.model.M):
            self.assertEqual(self.model.N[d], len(self.model.z[d]))
            for n in range(self.model.N[d]):
                self.assertGreaterEqual(self.model.z[d][n], 0)
                self.assertLess(self.model.z[d][n], self.T)


class TestCLDA(TestLDA):

    def setUp(self):
        self.corpus = TestCLDA.corpus
        self.K = 2
        self.T = 10
        self.gamma = 2
        self.alpha = .5
        self.beta = .01
        self.model = ClusterLDA(self.corpus, self.K, self.T,
                                self.gamma, self.alpha, self.beta)

    def test_model(self):
        TestLDA.test_model(self)

        self.assertEqual(self.K, self.model.K)
        self.assertEqual(self.gamma, self.model.gamma)

        self.assertEqual(self.model.M, len(self.model.k))
        for d in range(self.model.M):
            self.assertGreaterEqual(self.model.k[d], 0)
            self.assertLess(self.model.k[d], self.K)


class TestMixMulti(TestModel):

    def setUp(self):
        self.corpus = TestMixMulti.corpus
        self.K = 2
        self.gamma = 2
        self.beta = .01
        self.model = MixtureMultinomial(self.corpus, self.K,
                                        self.gamma, self.beta)

    def test_model(self):
        TestModel.test_model(self)

        self.assertEqual(self.K, self.model.K)
        self.assertEqual(self.gamma, self.model.gamma)

        self.assertEqual(self.model.M, len(self.model.k))
        for d in range(self.model.M):
            self.assertGreaterEqual(self.model.k[d], 0)
            self.assertLess(self.model.k[d], self.K)

    def test_inference(self):
        TestModel.test_inference(self)
