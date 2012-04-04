"""Test the various pre-processes that can be done"""

import unittest
from pipeline.corpus import CorpusReader, Tokenizer
from pipeline.preprocess import rare_words

class TestRare(unittest.TestCase):

    def runTest(self):
        reader = CorpusReader(Tokenizer([]))
        reader.add_dir('test_data/lorum')
        corpus = reader.read()
        transformed = rare_words(corpus, 5)

        corpus_vocab = set(corpus.vocab)
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
