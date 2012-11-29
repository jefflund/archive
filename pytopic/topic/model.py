"""Base class for graphical model"""

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

        self._handlers = []

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
        for handler in self._handlers:
            handler.handle(self)

    def inference(self, iterations):
        """
        TopicModel.inference(int): return None
        Performs inference on the model for the given number of iterations
        """

        for handler in self._handlers:
            handler.restart()
        for _ in range(iterations):
            self.iteration()

    def print_state(self, verbose=False):
        """
        TopicModel.print_state(bool): return None
        Prints a summary of the current state of the sampled model
        """

    def register_handler(self, handler):
        self._handlers.append(handler)


def init_counter(*dims):
    """
    init_counter(*int): return matrix of int
    Returns a matrix of zeros with the specified dimensions
    """

    if len(dims) == 1:
        return [0] * dims[0]
    else:
        return [init_counter(*dims[1:]) for _ in range(dims[0])]


class IterationHandler(object):
    """Base class for handlers run at each iteration of inference"""

    def restart(self):
        """Called at the start of inference"""

        raise NotImplementedError()

    def handle(self, model):
        """Called at the conclusion of each iteration"""

        raise NotImplementedError()
