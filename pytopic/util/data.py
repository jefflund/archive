"""A collection of useful data structures and data loading functions"""

class Index(object):

    def __init__(self):
        self._tokens = []
        self._types = {}

    def insert_token(self, token):
        if token not in self._types:
            self._tokens.append(token)
            self._types[token] = self._tokens.index(token)
        return self._tokens.index(token)

    def convert_token(self, tokens):
        return [self.insert_token(token) for token in tokens]

    def token_type(self, token):
        return self._types[token]

    def token(self, token_type):
        self._tokens[token_type]

    def __iter__(self):
        return iter(self._tokens)

    def __len__(self):
        return len(self._tokens)

    def __getitem__(self, token_type):
        return self._tokens[token_type]

def load_stopwords(*stopword_files):
    stopwords = set()
    for filename in stopword_files:
        for word in open(filename):
            word = word.strip()
            if len(word) > 0:
                stopwords.add(word)
    return stopwords
