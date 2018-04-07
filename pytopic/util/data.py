"""A collection of useful data structures and data loading functions"""

import os
import errno
import pickle

class Index(object):
    """A mapping from unique token types, to the token symbols"""

    def __init__(self):
        self._tokens = []
        self._types = {}

    def add(self, token):
        """
        Index.add(str): int
        Returns the unique token type for the given token symbol, inserting a
        new symbol and token type if needed.
        """

        if token not in self._types:
            self._types[token] = len(self._tokens)
            self._tokens.append(token)
        return self._types[token]

    def add_unique(self, token):
        """
        Index.add_unique(str): int
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
        Index.convert_tokens(iterable of str): list of int
        Converts a list of token symbols to token types, inserting tokens as
        needed.
        """

        return [self.add(token) for token in tokens]

    def token_type(self, token):
        """
        Index.token_type(str): int
        Returns the token type of the given token symbol, raising a KeyError
        if the given token has not been previously inserted.
        """

        return self._types[token]

    def token_symbol(self, token_type):
        """
        Index.token_symbol(int): str
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
        self.filelist = set()

    def add_file(self, filename):
        """
        Reader.add_file(str): None
        Adds a single filename to the set of files to be read
        """

        if not os.path.exists(filename):
            raise RuntimeError('{} does not exist'.format(filename))

        self.filelist.add(filename)

    def add_dir(self, dirpath):
        """
        Reader.add_dir(str): None
        Adds files in a directory (recursive) to the set of files to be read
        """

        if not os.path.exists(dirpath):
            raise RuntimeError('{} does not exist'.format(dirpath))
        elif not os.path.isdir(dirpath):
            raise RuntimeError('{} is not a directory'.format(dirpath))

        for root, dirs, files in os.walk(dirpath):
            self.filelist.update(os.path.join(root, f) for f in files)

    def add_index(self, indexname, data_dir=''):
        """
        Reader.add_index(str, str): None
        Adds the files from an index file to the Reader. The index file should
        have one file per line. The data dir gives the root directory from
        which the index file indexes
        """

        with open(indexname) as filelist:
            for filename in filelist:
                filename = filename.strip()
                if len(filename)> 0:
                    self.add_file(os.path.join(data_dir, filename))

    def add_index_dir(self, dirpath, data_dir=''):
        """
        Reader.add_index_dir(str. str): None
        Treats each file in the directory as an index file and addes the
        indicated files to the Reader.
        """

        for root, _, files in os.walk(dirpath):
            for indexfile in files:
                self.add_index(os.path.join(root, indexfile), data_dir)

    def get_files(self):
        """
        Reader.get_files(): generator of tuple
        Yields tuples of filenames and file objects for each of the filenames
        which are to be read
        """

        for filename in sorted(self.filelist):
            with open(filename) as buff:
                yield filename, buff

    def read(self):
        """
        Reader.read(): object
        Reads all of the files in the reader and constructs a data object
        """

        raise NotImplementedError()


def init_counter(*dims):
    """
    init_counter(*int): matrix of int
    Returns a matrix of zeros with the specified dimensions
    """

    if len(dims) == 1:
        return [0] * dims[0]
    else:
        return [init_counter(*dims[1:]) for _ in range(dims[0])]


def ensure_dirs(path):
    """
    ensure_dirs(str): None
    Will create the directories for a particular file path. Unlike os.makedirs
    ensure_dirs will not fail the path already exists.
    """

    dirpath = os.path.dirname(path)
    try:
        os.makedirs(dirpath)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise


def pickle_cache(pickle_path):
    """
    pickle_cache(str): decorator
    Creates a decorator which caches the results of a function
    to the specified pickle file. If the function is called a second time, then
    the cached result is returned. Note that this caching occurs even if the
    arguments to the function are changed.
    """

    def cache(data_func):
        def load_data(*args, **kwargs):
            if os.path.exists(pickle_path):
                return pickle.load(open(pickle_path))
            else:
                data = data_func(*args, **kwargs)
                ensure_dirs(pickle_path)
                pickle.dump(data, open(pickle_path, 'w'))
                return data
        return load_data
    return cache
