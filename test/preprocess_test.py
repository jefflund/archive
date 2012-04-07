"""Test the various pre-processes that can be done"""

from pipeline.corpus import CorpusReader, Tokenizer
from pipeline.preprocess import rare_words, stopwords
from util.data import load_stopwords
import unittest

class PreprocessRare(unittest.TestCase):

    def setUp(self):
        reader = CorpusReader(Tokenizer())
        reader.add_dir('test_data/lorum')
        self.corpus = reader.read()

    def test_stopwords(self):
        stopwords_set = load_stopwords('test_data/latin_stop')
        transformed = stopwords(self.corpus, stopwords_set)
        transformed_vocab = set(transformed.vocab)
        self.assertNotIn('et', transformed_vocab)
        self.assertNotIn('sed', transformed_vocab)
        self.assertNotIn('ut', transformed_vocab)
        self.assertNotIn('in', transformed_vocab)

    def test_rare_words(self):

        transformed = rare_words(self.corpus, 5)

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
