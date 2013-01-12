"""Basic generative model of text, including vanilla LDA"""

class TopicModel(object):
    """Base class for a generative model of textual data"""

    def __init__(self, corpus):
        self.titles = list(corpus.titles)
        self.vocab = list(corpus.vocab)
        self.w = [doc for doc in corpus]

        self.M = len(self.w)
        self.N = [len(doc) for doc in self.w]
        self.V = len(self.vocab)

        self._handlers = []

        self._inference_algorithm = None

    def sample(self):
        """
        TopicModel.sample(): return None
        Performs a single iteration of sampling.
        """

    def inference(self, iterations):
        """
        TopicModel.inference(int): return None
        Performs inference on the model for the given number of iterations
        """

        assert self._inference_algorithm is not None

        for handler in self._handlers:
            handler.restart()

        for _ in range(iterations):
            self._inference_algorithm()
            for handler in self._handlers:
                handler.handle(self)

    def set_inference(self, algorithm):
        self._inference_algorithm = algorithm(self)

    def print_state(self, verbose=False):
        """
        TopicModel.print_state(bool): return None
        Prints a summary of the current state of the sampled model
        """

    def register_handler(self, handler):
        self._handlers.append(handler)


class IterationHandler(object):
    """Base class for handlers run at each iteration of inference"""

    def restart(self):
        """Called at the start of inference"""

    def handle(self, model):
        """Called at the conclusion of each iteration"""

        raise NotImplementedError()

class InferenceAlgorithm(object):
    """Represents a class of inference algorithm"""

    def __new__(cls, model):
        implementation = cls.implementations[type(model)]
        return implementation(model)
