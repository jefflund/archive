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


class ClusterConvergeceCheck(basic.IterationHandler):
    """Raises a StopIteration exception if the clustering model converges"""

    def __init__(self, init_state):
        self.last_state = [k for k in init_state]

    def handle(self, model):
        model_changed = False
        for d, k_d in enumerate(model.k):
            if self.last_state[d] != k_d:
                self.last_state[d] = k_d
                model_changed = True
        if not model_changed:
            raise StopIteration()


class MetricPrinter(basic.IterationHandler):
    """Evaluates a clustering model using three external metrics"""

    def __init__(self, iter_interval, gold_clustering):
        self.iter_interval = iter_interval
        self.gold_clustering = gold_clustering

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            pred = cluster.Clustering.from_model(model)
            contingency = cluster.Contingency(self.gold_clustering, pred)

            print 'ARI', cluster.ari(contingency)
            print 'FM', cluster.f_measure(contingency)
            print 'VI', cluster.variation_info(contingency)
            print 'Likelihood', model.likelihood()


class StatePrinter(basic.IterationHandler):
    """Prints the string representation of a state variable"""

    def __init__(self, iter_interval, state):
        self.iter_interval = iter_interval
        self.state = state

    def handle(self, model):
        if model.num_iters % self.iter_interval == 0:
            print repr(self.state)


class StateTimePrinter(basic.IterationHandler):

    def __init__(self, state):
        self.state = state
        self.start_time = time.time()

    def handle(self, model):
        print time.time() - self.start_time, repr(self.state)

