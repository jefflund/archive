"""A collection of useful data structures and data loading functions"""

import os
import pickle

class Index(object):
    """A mapping from unique token types, to the token symbols"""

    def __init__(self):
        self._tokens = []
        self._types = {}

    def add(self, token):
        """
        Index.add(str): return int
        Returns the unique token type for the given token symbol, inserting a
        new symbol and token type if needed.
        """

        if token not in self._types:
            self._types[token] = len(self._tokens)
            self._tokens.append(token)
        return self._types[token]

    def add_unique(self, token):
        """
        Index.add_unique(str): return int
        Returns the unique token type for the given token symbol, but raises a
        KeyError if the token has already been inserted.
        """

        if token in self._types:
            raise KeyError()
        else:
            token_type = len(self._tokens)
            self._types[token] = token_type
            self._tokens.append(token)
            return token_type

    def convert_tokens(self, tokens):
        """
        Index.convert_tokens(iterable of str): return list of int
        Converts a list of token symbols to token types, inserting tokens as
        needed.
        """

        return [self.add(token) for token in tokens]

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


class Reader(object):
    """Base class for data import from text files"""

    def __init__(self):
        self.filelist = []

    def add_file(self, filename):
        """
        Reader.add_file(str): return None
        Adds a single filename to the list of files to be read
        """

        self.filelist.append(filename)

    def add_dir(self, dirpath):
        """
        Reader.add_dir(str): return None
        Adds files in a directory (recursive) to the list of files to be read
        """

        for root, dirs, files in os.walk(dirpath):
            dirs.sort()
            files = [os.path.join(root, f) for f in sorted(files)]
            self.filelist.extend(files)

    def get_files(self):
        """
        Reader.get_files(): return generator of tuple
        Yields tuples of filenames and file objects for each of the filenames
        which are to be read
        """

        for filename in self.filelist:
            with open(filename) as buff:
                yield filename, buff

    def read(self):
        """
        Reader.read(): return object
        Reads all of the files in the reader and constructs a data object
        """

        raise NotImplementedError()


def init_counter(*dims):
    """
    init_counter(*int): return matrix of int
    Returns a matrix of zeros with the specified dimensions
    """

    if len(dims) == 1:
        return [0] * dims[0]
    else:
        return [init_counter(*dims[1:]) for _ in range(dims[0])]


def pickle_cache(pickle_path):
    """
    pickle_cache(str): decorator
    Creates a decorator which caches the results of a parameterless function
    to the specified pickle file.
    """

    def cache(data_func):
        def load_data(*args, **kwargs):
            if os.path.exists(pickle_path):
                return pickle.load(open(pickle_path))
            else:
                data = data_func(*args, **kwargs)
                pickle.dump(data, open(pickle_path, 'w'))
                return data
        return load_data
    return cache
