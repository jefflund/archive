"""Basic generative model of text, including vanilla LDA"""

import sys
import time

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
        self.num_iters = 0

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

        self._ensure_inference_set()

        for _ in range(iterations):
            try:
                self.iteration()
            except StopIteration:
                break

    def timed_inference(self, seconds):
        """
        TopicModel.timed_inference(int): return None
        Performs inference on the model for the given number of seconds
        """

        self._ensure_inference_set()

        end_time = time.time() + seconds

        while time.time() < end_time:
            try:
                self.iteration()
            except StopIteration:
                break

    def iteration(self):
        self._inference_algorithm()
        self.num_iters += 1
        for handler in self._handlers:
            handler.handle(self)

    def set_inference(self, algorithm, *params):
        """
        TopicModel.set_inference(str): return None
        Sets the algorithm to be used for inference on the model. Currently the
        only valid algorithms are: gibbs.
        """

        self._inference_algorithm = self.algorithms[algorithm](self, *params)

    def _ensure_inference_set(self):
        if self._inference_algorithm is None:
            self.set_inference(self.default_algorithm)

    def print_state(self, verbose=False):
        """
        TopicModel.print_state(bool): return None
        Prints a summary of the current state of the sampled model
        """

        raise NotImplementedError()

    def register_handler(self, handler):
        self._handlers.append(handler)


class IterationHandler(object):
    """Base class for handlers run at each iteration of inference"""

    def handle(self, model):
        """Called at the conclusion of each iteration"""

        raise NotImplementedError()
