"""Base class for graphical model"""

class TopicModel(object):
    """Base class for a topic model of textual data"""

    def __init__(self, corpus):
        self.titles = list(corpus.titles)
        self.vocab = list(corpus.vocab)
        self.w = [doc for doc in corpus]

        self.M = len(self.w)
        self.N = [len(doc) for doc in self.w]
        self.V = len(self.vocab)

    def iteration(self):
        """
        TopicModel.iteration(): return None
        Performs a single iteration of inference
        """

    def inference(self, iterations):
        """
        TopicModel.inference(int): return None
        Performs inference on the model for the given number of iterations
        """

        for _ in range(iterations):
            self.iteration()

def top_n(counts, n):
    """
    top_n(list of int, int): return list of int
    Returns the indices of the top n counts in the given list of counts, 
    excluding any zero counts.
    """

    counts = [(i, c) for i, c in enumerate(counts) if c > 0]
    return sorted(counts, key=lambda x: x[1], reverse=True)[:n]
