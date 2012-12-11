"""Basic generative model of text, including vanilla LDA"""

from pytopic.util.compute import sample_uniform, sample_counts, top_n
from pytopic.util.data import init_counter

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


class IterationHandler(object):
    """Base class for handlers run at each iteration of inference"""

    def restart(self):
        """Called at the start of inference"""

    def handle(self, model):
        """Called at the conclusion of each iteration"""

        raise NotImplementedError()


class VanillaLDA(TopicModel):
    """Latent Dirichlet Allocation with a Gibbs sampler"""

    def __init__(self, corpus, T, alpha, beta):
        TopicModel.__init__(self, corpus)

        self.T = T
        self.alpha = alpha
        self.beta = beta
        self.Vbeta = self.V * self.beta

        self.z = [[0 for _ in range(size)] for size in self.N]

        self.c_t = init_counter(self.T)
        self.c_dt = init_counter(self.M, self.T)
        self.c_tv = init_counter(self.T, self.V)

        for d in range(self.M):
            for n in range(self.N[d]):
                self.set_z(d, n, sample_uniform(self.T))

    def sample(self):
        for d in range(self.M):
            for n in range(self.N[d]):
                self.sample_z(d, n)

    def sample_z(self, d, n):
        """
        VanillaLDA.sample_z(int, int): return None
        Samples the value of z_dn according to the complete conditional
        """

        self.unset_z(d, n)
        counts = [self.prob_z(d, n, j) for j in range(self.T)]
        self.set_z(d, n, sample_counts(counts))

    def prob_z(self, d, n, j):
        """
        VanillaLDA.prob_z(int, int, int): return float
        Returns the probability p(z_dn=j|w, z_-dn, alpha, beta)
        """

        prob = self.alpha + self.c_dt[d][j]
        prob *= self.beta + self.c_tv[j][self.w[d][n]]
        prob /= self.Vbeta + self.c_t[j]
        return prob

    def set_z(self, d, n, z_dn):
        """
        VanillaLDA.set_z(int, int, int): return None
        Sets the value of z_dn and updates the counters. Does not adjust the
        counters for the previous value of z_dn.
        """

        self.z[d][n] = z_dn

        self.c_t[z_dn] += 1
        self.c_dt[d][z_dn] += 1
        self.c_tv[z_dn][self.w[d][n]] += 1

    def unset_z(self, d, n):
        """
        VanillaLDA.unset_z(int, int): return None
        Adjust the counters so that z_dn is not used in the counts.
        """

        z_dn = self.z[d][n]

        self.c_t[z_dn] -= 1
        self.c_dt[d][z_dn] -= 1
        self.c_tv[z_dn][self.w[d][n]] -= 1

    def topic_words(self, t, n):
        """
        VanillaLDA.topic_words(int, int): return list of int
        Returns the top n words in topic t
        """

        return top_n(self.c_tv[t], n)

    def doc_topics(self, d, n):
        """
        VanillaLDA.doc_topics(int, int): return list of int
        Returns the top n topics in document d
        """

        return top_n(self.c_dt[d], n)

    def print_state(self, verbose=False):
        for t in range(self.T):
            print '{0} -'.format(t),
            for v in self.topic_words(t, 15):
                print self.vocab[v],
            print

        if verbose:
            for d in range(self.M):
                print '{0} -'.format(self.titles[d]),
                for t in self.doc_topics(d, 5):
                    print '{0}('.format(t),
                    for v in self.topic_words(t, 2):
                        print self.vocab[v],
                    print ')',
                print

        print
