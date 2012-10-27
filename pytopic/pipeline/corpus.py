"""Basic functionality for loading a text corpus from file"""

import os
import re
from pytopic.util.data import Index

class Corpus(object):
    """A collection of text documents"""

    def __init__(self):
        self.vocab = Index()
        self.titles = []
        self.data = []

    def add_document(self, title, tokens):
        """
        Corpus.add_document(str, iterable of str): return None
        Adds a new document to the Corpus. If the title is not unique, the old
        document with that title will be overridden.
        """

        self.titles.append(title)
        self.data.append(self.vocab.convert_tokens(tokens))

    def get_text(self, doc_index):
        """
        Corpus.get_text(int): return list of str
        Returns the token symbols in the given document
        """

        return [self.vocab[i] for i in self.data[doc_index]]

    def __getitem__(self, doc_index):
        return self.data[doc_index]

    def __len__(self):
        return len(self.data)

    def __iter__(self):
        return iter(self.data)


class CorpusReader(object):
    """Facilitates the construction of text corpora from files"""

    def __init__(self, tokenizer=None):
        self.filelist = []
        self.tokenizer = Tokenizer() if tokenizer is None else tokenizer

    def add_file(self, filename):
        """
        CorpusReader.add_file(str): return None
        Adds a single filename to the list of files to be read
        """

        self.filelist.append(filename)

    def add_dir(self, dirpath):
        """
        CorpusReader.add_dir(str): return None
        Adds files in a directory (recursive) to the list of files to be read
        """

        for root, dirs, files in os.walk(dirpath):
            dirs.sort()
            files = [os.path.join(root, f) for f in sorted(files)]
            self.filelist.extend(files)

    def get_files(self):
        """
        CorpusReader.get_files(): return generator of tuple
        Yields tuples of filenames and file objects for each of the filenames
        which are to be read
        """

        for filename in self.filelist:
            with open(filename) as buff:
                yield filename, buff

    def read(self):
        """
        CorpusReader.read(): return Corpus
        Reads all of the files in the reader and constructs a new Corpus
        """

        corpus = Corpus()
        for filename, buff in self.get_files():
            for title, tokens in self.tokenizer.tokenize(filename, buff):
                corpus.add_document(title, tokens)
        return corpus


class Tokenizer(object):
    """Performs tokenization for a CorpusReader"""

    def __init__(self, split_re='\s+', filter_re='[^a-zA-Z]'):
        self.split_re = re.compile(split_re)
        self.filter_re = re.compile(filter_re)

    def tokenize(self, filename, buff):
        """
        Tokenizer.tokenize(str, file): return generator of tuple
        Using the filename and file buffer, yields tuples of string titles and
        list of token symbols representing documents.
        """

        tokens = self.split_re.split(buff.read())
        tokens = [self.transform(token) for token in tokens]
        tokens = [token for token in tokens if self.keep(token)]
        yield filename, tokens

    def transform(self, token):
        """
        Tokenizer.transform(str): return str
        Transforms a string into the token symbol that should be used
        """

        return self.filter_re.sub('', token.lower())

    def keep(self, token):
        """
        Tokenizer.keep(str): return bool
        Returns true if the token symbol should be kept by the CorpusReader
        """

        return 2 < len(token) < 10
