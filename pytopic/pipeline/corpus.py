"""Basic functionality for loading a text corpus from file"""

import os
import re
from util.data import Index

class Corpus(object):

    def __init__(self):
        self.vocab = Index()
        self.titles = Index()
        self.data = {}

    def add_document(self, title, tokens):
        doc_index = self.titles.insert_token(title)
        self.data[doc_index] = self.vocab.convert_token(tokens)

    def get_text(self, doc_index):
        return [self.vocab[i] for i in self.data[doc_index]]

    def __getitem__(self, doc_index):
        return self.data[doc_index]

    def __len__(self):
        return len(self.data)

    def __iter__(self):
        for doc_index in sorted(self.data.keys()):
            yield self.data[doc_index]


class CorpusReader(object):

    def __init__(self, tokenizer):
        self.filelist = []
        self.tokenizer = tokenizer

    def add_file(self, filename):
        self.filelist.append(filename)

    def add_dir(self, dirpath):
        for root, dirs, files in os.walk(dirpath):
            dirs.sort()
            files = [os.path.join(root, f) for f in sorted(files)]
            self.filelist.extend(files)

    def get_files(self):
        for filename in self.filelist:
            with open(filename) as buff:
                yield filename, buff

    def read(self):
        corpus = Corpus()
        for filename, buff in self.get_files():
            for title, tokens in self.tokenizer.tokenize(filename, buff):
                corpus.add_document(title, tokens)
        return corpus


class Tokenizer(object):

    def __init__(self, stopwords, split_re='\s+', filter_re='[^a-zA-Z]'):
        self.stopwords = stopwords
        self.split_re = re.compile(split_re)
        self.filter_re = re.compile(filter_re)

    def tokenize(self, filename, buff):
        tokens = self.split_re.split(buff.read())
        tokens = [self.transform(token) for token in tokens]
        tokens = [token for token in tokens if self.keep(token)]
        yield filename, tokens

    def transform(self, token):
        return self.filter_re.sub('', token.lower())

    def keep(self, token):
        return (token not in self.stopwords and
                len(token) > 2 and len(token) < 10)
