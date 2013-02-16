import time
import pickle
from pytopic.model.basic import IterationHandler
from pytopic.analysis.cluster import (Clustering, Contingency,
                                      f_measure, ari, variation_info)


class Printer(IterationHandler):
    """Calls print_state on the model at a specified iteration interval"""

    def __init__(self, iter_interval, verbose=False):
        self.iter_interval = iter_interval
        self.verbose = verbose

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            model.print_state(self.verbose)


class Timer(IterationHandler):
    """Prints the timing of each iteration"""

    def __init__(self):
        self.last_time = time.time()

    def handle(self, model):
        curr_time = time.time()
        iter_time = curr_time - self.last_time
        self.last_time = curr_time
        print '{0} {1}'.format(model.num_iters, iter_time)


class Checkpointer(IterationHandler):
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


class ClusterMetrics(IterationHandler):
    """Evaluates a clustering model using three external metrics"""

    def __init__(self, gold_clustering, iter_interval):
        self.gold_clustering = gold_clustering
        self.iter_interval = iter_interval
        self.best_ari = float('-inf')
        self.best_fm = float('-inf')
        self.best_vi = float('inf')

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            contingency = self.get_contingency(model)
            curr_ari = ari(contingency)
            curr_fm = f_measure(contingency)
            curr_vi = variation_info(contingency)

            print 'ARI {}'.format(max(curr_ari, self.best_ari))
            print 'FM {}'.format(max(curr_fm, self.best_fm))
            print 'VI {}'.format(min(curr_vi, self.best_vi))

    def restart(self, model):
        contingency = self.get_contingency(model)
        self.best_ari = max(self.best_ari, ari(contingency))
        self.best_fm = max(self.best_fm, f_measure(contingency))
        self.best_vi = min(self.best_vi, variation_info(contingency))

    def get_contingency(self, model)
        pred_clustering = Clustering.from_model(model)
        return Contingency(self.gold_clustering, pred_clustering)


class MalletOutput(IterationHandler):
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


class Perplexity(IterationHandler):
    """Calculates and prints the perplexity of a test dataset"""

    def __init__(self, test_corpus, iter_interval):
        self.test_corpus = test_corpus
        self.iter_interval = iter_interval
        self.best_perplex = float('inf')

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            curr_perplex = model.perplexity(self.test_corpus)
            print 'Perplexity {}'.format(min(curr_perplex, self.best_perplex))

    def restart(self, model):
        curr_perplex = model.perplexity(self.test_corpus)
        self.best_perplex = min(curr_perplex, self.best_perplex)


class ClusterConvergeceCheck(IterationHandler):

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
