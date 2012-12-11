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
        self.curr_interval = 0

    def handle(self, model):
        self.curr_interval += 1
        if self.curr_interval % self.iter_interval == 0:
            model.print_state(self.verbose)


class Timer(IterationHandler):
    """Prints the timing of each iteration"""

    def __init__(self):
        self.curr_interval = 0
        self.restart()

    def restart(self):
        self.last_time = time.time()

    def handle(self, model):
        self.curr_interval += 1
        curr_time = time.time()
        iter_time = curr_time - self.last_time
        self.last_time = curr_time
        print '{0} {1}'.format(self.curr_interval, iter_time)


class Checkpointer(IterationHandler):
    """Pickles the model at the specified time interval"""

    def __init__(self, time_interval, filename):
        self.time_interval = time_interval
        self.filename = filename
        self.restart()

    def restart(self):
        self.last_time = time.time()

    def handle(self, model):
        curr_time = time.time()
        if self.last_time - curr_time >= self.time_interval:
            self.last_time = curr_time
            with open(self.filename, 'w') as outfile:
                pickle.dump(model, outfile)


class ClusterMetrics(IterationHandler):

    def __init__(self, gold_clustering, iter_interval):
        self.gold_clustering = gold_clustering
        self.iter_interval = iter_interval
        self.curr_interval = 0

    def handle(self, model):
        self.curr_interval += 1
        if self.curr_interval % self.iter_interval == 0:
            pred_clustering = Clustering.from_model(model)
            contingency = Contingency(self.gold_clustering, pred_clustering)
            print 'ARI {}'.format(ari(contingency))
            print 'FM {}'.format(f_measure(contingency))
            print 'VI {}'.format(variation_info(contingency))
