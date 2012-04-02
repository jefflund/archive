"""Test for the Index class and other data helpers"""

import unittest
from pytopic.util.data import Index, load_stopwords

class TestIndex(unittest.TestCase):

    def setUp(self):
        self.index = Index()

    def test_insert(self):
        spam = self.index.insert_token('spam')
        ham = self.index.insert_token('ham')
        eggs = self.index.insert_token('eggs')

        self.assertEqual(spam, self.index.insert_token('spam'))
        self.assertEqual(spam, self.index.token_type('spam'))
        self.assertEqual('spam', self.index[spam])
        self.assertEqual('spam', self.index.token_symbol(spam))

        self.assertEqual(ham, self.index.insert_token('ham'))
        self.assertEqual(ham, self.index.token_type('ham'))
        self.assertEqual('ham', self.index[ham])
        self.assertEqual('ham', self.index.token_symbol(ham))

        self.assertEqual(eggs, self.index.insert_token('eggs'))
        self.assertEqual(eggs, self.index.token_type('eggs'))
        self.assertEqual('eggs', self.index[eggs])
        self.assertEqual('eggs', self.index.token_symbol(eggs))

        self.assertEqual(3, len(self.index))
        self.assertSequenceEqual(['spam', 'ham', 'eggs'], list(self.index))

    def test_convert(self):
        self.index.insert_token('spam')
        self.index.insert_token('ham')
        tokens = ['spam', 'eggs', 'ham', 'yummy']
        types = self.index.convert_tokens(tokens)

        for i in range(len(tokens)):
            self.assertEqual(self.index.token_type(tokens[i]), types[i])

class TestStopwords(unittest.TestCase):

    def runTest(self):
        stopwords1 = {'a', 'of', 'the', 'and'}
        stopwords2 = {'there', 'the', 'here', 'by'}
        self.assertEqual(stopwords1, load_stopwords('test_data/stopwords1'))
        self.assertEqual(stopwords2, load_stopwords('test_data/stopwords2'))
        joint = load_stopwords('test_data/stopwords1', 'test_data/stopwords2')
        self.assertEqual(stopwords1 | stopwords2, joint)
