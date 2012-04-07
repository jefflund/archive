"""Test of Corpus and corpus loading functionality"""

from pipeline.corpus import Tokenizer, CorpusReader, Corpus
import StringIO
import unittest
from util.data import load_stopwords

class TestTokenizer(unittest.TestCase):

    def setUp(self):
        self.tokenizer = Tokenizer()

    def test_keep(self):
        self.assertFalse(self.tokenizer.keep(''))
        self.assertFalse(self.tokenizer.keep('a'))
        self.assertFalse(self.tokenizer.keep('at'))
        self.assertFalse(self.tokenizer.keep('foobarbazs'))

        self.assertTrue(self.tokenizer.keep('spam'))
        self.assertTrue(self.tokenizer.keep('eggs'))
        self.assertTrue(self.tokenizer.keep('ham'))

    def test_filter(self):
        self.assertEqual('spam', self.tokenizer.transform('SPAM'))
        self.assertEqual('eggs', self.tokenizer.transform('eggs$(*&'))
        self.assertEqual('ham', self.tokenizer.transform('*&(*&H3443a(*&m.'))

    def test_tokenize(self):
        buff = StringIO.StringIO('SPAM eggs&*\nH@Am\nfoo bar$# 234')
        docs = list(self.tokenizer.tokenize('python', buff))
        self.assertEqual(1, len(docs))
        title, words = docs[0]
        self.assertEqual('python', title)
        self.assertListEqual(['spam', 'eggs', 'ham', 'foo', 'bar'], words)


class TestReader(unittest.TestCase):

    def setUp(self):
        self.reader = CorpusReader(Tokenizer())

    def test_file(self):
        files = ['test_data/lorum/lorum0.txt', 'test_data/lorum/lorum1.txt']
        for filename in files:
            self.reader.add_file(filename)
        files = [(filename, open(filename).read()) for filename in files]
        self.reader.get_files()
        retrieved = [(filename, buff.read())
                     for filename, buff in self.reader.get_files()]
        self.assertListEqual(files, retrieved)

    def test_dir(self):
        self.reader.add_dir('test_data/lorum')
        files = ['test_data/lorum/lorum{}.txt'.format(i) for i in range(15)]
        files.sort()
        files = [(filename, open(filename).read()) for filename in files]
        self.reader.get_files()
        retrieved = [(filename, buff.read())
                     for filename, buff in self.reader.get_files()]
        self.assertListEqual(files, retrieved)

    def test_read(self):
        self.assertIsInstance(self.reader.read(), Corpus)


class TestCorpus(unittest.TestCase):

    def setUp(self):
        reader = CorpusReader(Tokenizer())
        reader.add_dir('test_data/lorum')
        self.corpus = reader.read()

        self.data = {}
        for i in range(15):
            title = 'test_data/lorum/lorum{}.txt'.format(i)
            text = open(title).read().strip().split()
            text = [word.lower() for word in text]
            text = [word.replace('.', '') for word in text]
            text = [word.replace(',', '') for word in text]
            text = [word.replace(';', '') for word in text]
            text = [word for word in text if len(word) > 2]
            text = [word for word in text if len(word) < 10]
            self.data[title] = text

    def test_data(self):
        for title, text in self.data.iteritems():
            doc_index = self.corpus.titles.token_type(title)
            self.assertListEqual(text, self.corpus.get_text(doc_index))

    def test_vocab(self):
        for title, text in self.data.iteritems():
            doc_index = self.corpus.titles.token_type(title)
            for i, token in enumerate(self.corpus.data[doc_index]):
                self.assertEqual(self.corpus.vocab[token], text[i])
