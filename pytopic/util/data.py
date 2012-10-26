"""A collection of useful data structures and data loading functions"""

class Index(object):
    """A mapping from unique token types, to the token symbols"""

    def __init__(self):
        self._tokens = []
        self._types = {}

    def insert_token(self, token):
        """
        Index.insert_token(str): return int
        Returns the unique token type for the given token symbol, inserting a
        new symbol and token type if needed.
        """

        if token not in self._types:
            self._types[token] = len(self._tokens)
            self._tokens.append(token)
        return self._types[token]

    def convert_tokens(self, tokens):
        """
        Index.convert_tokens(iterable of str): return list of int
        Converts a list of token symbols to token types, inserting tokens as
        needed.
        """

        return [self.insert_token(token) for token in tokens]

    def token_type(self, token):
        """
        Index.token_type(str): return int
        Returns the token type of the given token symbol, raising a KeyError
        if the given token has not been previously inserted.
        """

        return self._types[token]

    def token_symbol(self, token_type):
        """
        Index.token_symbol(int): return str
        Returns the token symbol for the given token type, raising a KeyError
        if the given token type has not been used.
        """

        return self._tokens[token_type]

    def __iter__(self):
        return iter(self._tokens)

    def __len__(self):
        return len(self._tokens)

    def __getitem__(self, token_type):
        return self._tokens[token_type]


def load_stopwords(*stopword_filenames):
    """
    load_stopwords(*str): return set of str
    Reads stopword files with one stopword per line into a set
    """

    stopwords = set()
    for filename in stopword_filenames:
        for word in open(filename):
            word = word.strip()
            if len(word) > 0:
                stopwords.add(word)
    return stopwords
