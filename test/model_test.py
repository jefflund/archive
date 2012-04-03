"""Ensures that topic models do not crash - results to be verified by hand"""

import unittest
from pipeline.corpus import CorpusReader, Tokenizer
from util.data import load_stopwords
from topic.model import TopicModel
from topic.vanilla import VanillaLDA

class TestModel(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        stopwords = load_stopwords('test_data/latin_stop')
        reader = CorpusReader(Tokenizer(stopwords))
        reader.add_dir('test_data/lorum')
        reader.add_dir('test_data/ipsum')
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
        self.model.iteration = inc
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

    def test_inference(self, print_state=False):
        TestModel.test_inference(self)
        if print_state:
            self.model.print_state()
