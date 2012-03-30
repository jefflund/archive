"""Basic functionality for loading a text corpus from file"""

from util.data import Index

class Corpus(object):

    def __init__(self):
        self.vocab = Index()
        self.data = []

    def add_document(self, title, tokens):
        doc = title, self.vocab.convert_token(tokens)
        self.data.append(doc)

    def __len__(self):
        return len(self.data)
