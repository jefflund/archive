"""Base class for graphical model"""

import timeit

class TopicModel(object):
    """Base class for a topic model of textual data"""

    # forgive the horrible variable names - they match my white board...

    # w_dn = the token type of the nth word of the dth document
    # M = the number of documents
    # N_d = the number of tokens in document d
    # V = the number of unique token types

    def __init__(self, corpus):
        self.titles = list(corpus.titles)
        self.vocab = list(corpus.vocab)
        self.w = [doc for doc in corpus]

        self.M = len(self.w)
        self.N = [len(doc) for doc in self.w]
        self.V = len(self.vocab)

    def sample(self):
        """
        TopicModel.sample(): return None
        Performs a single iteration of sampling.
        """

    def iteration(self):
        """
        TopicModel.iteration(): return None
        Performs a single iteration of inference, along with output.
        """

        self.sample()

    def inference(self, iterations):
        """
        TopicModel.inference(int): return None
        Performs inference on the model for the given number of iterations
        """

        for _ in range(iterations):
            self.iteration()

    def print_state(self, verbose=False):
        """
        TopicModel.print_state(bool): return None
        Prints a summary of the current state of the sampled model
        """


def top_n(counts, n):
    """
    top_n(list of int, int): return list of int
    Returns the indices of the top n counts in the given list of counts, 
    excluding any zero counts.
    """

    keys = [i for i in range(len(counts)) if counts[i] > 0]
    return sorted(keys, key=lambda x: counts[x], reverse=True)[:n]
