import time
import pickle
from pytopic.model import basic
from pytopic.analysis import cluster

class Printer(basic.IterationHandler):
    """Calls print_state on the model at a specified iteration interval"""

    def __init__(self, iter_interval, verbose=False):
        self.iter_interval = iter_interval
        self.verbose = verbose

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            model.print_state(self.verbose)


class Timer(basic.IterationHandler):
    """Prints the timing of each iteration"""

    def __init__(self):
        self.last_time = time.time()

    def handle(self, model):
        curr_time = time.time()
        iter_time = curr_time - self.last_time
        self.last_time = curr_time
        print '{0} {1}'.format(model.num_iters, iter_time)


class Checkpointer(basic.IterationHandler):
    """Pickles the model at the specified time interval"""

    def __init__(self, time_interval, filename):
        self.time_interval = time_interval
        self.filename = filename
        self.last_time = time.time()

    def handle(self, model):
        curr_time = time.time()
        if self.last_time - curr_time >= self.time_interval:
            self.last_time = curr_time
            with open(self.filename, 'w') as outfile:
                pickle.dump(model, outfile)


class MalletOutput(basic.IterationHandler):
    """Writes a model to file in Mallet output format"""

    def __init__(self, iter_interval, filename):
        self.iter_interval = iter_interval
        self.filename = filename

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            with open(self.filename, 'w') as outfile:
                self.write_mallet(outfile, model)

    def write_mallet(self, outfile, model):
        """
        MalletOutput.write_mallet(file, model): return None
        Writes a model to the given file in Mallet output format
        """

        self.write_params(outfile, model)
        self.write_header(outfile)

        for d in range(model.M):
            title = model.titles[d]
            for n in range(model.N[d]):
                w = model.w[d][n]
                word = model.vocab[w]
                z = model.z[d][n]
                line = '{} {} {} {} {}\n'.format(d, title, n, w, word, z)
                outfile.write(line)

    def write_header(self, outfile):
        """
        MalletOutput.write_header(file): return None
        Writes a comment describing the Mallet output format
        """

        outfile.write('#doc source pos typeindex type topic\n')

    def write_params(self, outfile, model):
        """
        MalletOutput.write_params(file, model): return None
        Writes all int and float attributes with their value in comments
        """

        for attr in dir(model):
            value = getattr(model, attr)
            if type(value) is int or type(value) is float:
                outfile.write('#{} {}\n'.format(attr, value))


class ClusterConvergeceCheck(basic.IterationHandler):
    """Raises a StopIteration exception if the clustering model converges"""

    def __init__(self, init_state):
        self.last_state = [k for k in init_state]

    def handle(self, model):
        changes = 0
        for d, k_d in enumerate(model.k):
            if self.last_state[d] != k_d:
                self.last_state[d] = k_d
                changes += 1
        if changes == 0:
            raise StopIteration()


class MetricPrinter(basic.IterationHandler):
    """Evaluates a clustering model using three external metrics"""

    def __init__(self, gold_clustering, test_corpus, print_matrix=False):
        self.gold_clustering = gold_clustering
        self.test_corpus = test_corpus

        self.like = float('-inf')
        self.ari = float('-inf')
        self.fm = float('-inf')
        self.vi = float('inf')
        self.perp = float('inf')

        self.print_matrix = print_matrix

    def handle(self, model):
        contingency = None
        curr_like = model.likelihood()

        if curr_like > self.like:
            self.like = curr_like

            contingency = self.get_contingency(model)
            self.ari = cluster.ari(contingency)
            self.fm = cluster.f_measure(contingency)
            self.vi = cluster.variation_info(contingency)
            self.perp = model.perplexity(self.test_corpus)

        print 'ARI', self.ari
        print 'FM', self.fm
        print 'VI', self.vi
        print 'Likelihood', self.like
        print 'Perplexity', self.perp

        if self.print_matrix:
            if contingency is None:
                contingency = self.get_contingency()
            contingency.sort_contingency()
            contingency.print_contingency()

    def get_contingency(self, model):
        pred_clustering = cluster.Clustering.from_model(model)
        return cluster.Contingency(self.gold_clustering, pred_clustering)
