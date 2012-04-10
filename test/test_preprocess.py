"""Test the various pre-processes that can be done"""

from pipeline.corpus import CorpusReader
from pipeline.preprocess import filter_rarewords, filter_stopwords
from util.data import load_stopwords
import unittest

class PreprocessRare(unittest.TestCase):

    def setUp(self):
        reader = CorpusReader()
        reader.add_dir('test_data/lorum')
        self.corpus = reader.read()

    def test_stopwords(self):
        stopwords_set = load_stopwords('test_data/latin_stop')
        transformed = filter_stopwords(self.corpus, stopwords_set)
        transformed_vocab = set(transformed.vocab)
        self.assertNotIn('et', transformed_vocab)
        self.assertNotIn('sed', transformed_vocab)
        self.assertNotIn('ut', transformed_vocab)
        self.assertNotIn('in', transformed_vocab)

    def test_rare_words(self):

        transformed = filter_rarewords(self.corpus, 5)

        corpus_vocab = set(self.corpus.vocab)
        transformed_vocab = set(transformed.vocab)

        rare = ['dictum', 'conubia', 'fames', 'torquent',
                'ridiculus', 'viverra', 'vulputate', 'ridiculus']
        for word in rare:
            self.assertIn(word, corpus_vocab)
            self.assertNotIn(word, transformed_vocab)

        common = ['lacinia', 'integer', 'varius', 'dui', 'dolor']
        for word in common:
            self.assertIn(word, corpus_vocab)
            self.assertIn(word, transformed_vocab)
