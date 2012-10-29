import time
import pickle
from pytopic.topic.model import IterationHandler

class Printer(IterationHandler):

    def __init__(self, iter_interval, verbose=False):
        self.iter_interval = iter_interval
        self.verbose = verbose
        self.restart()

    def restart(self):
        self.curr_interval = 0

    def handle(self, model):
        self.curr_interval += 1
        if self.curr_interval % self.iter_interval == 0:
            model.print_state(self.verbose)


class Timer(IterationHandler):

    def __init__(self):
        self.restart()

    def restart(self):
        self.last_time = time.time()
        self.curr_interval = 0

    def handle(self, model):
        self.curr_interval += 1
        curr_time = time.time()
        print '{} - {}'.format(self.curr_interval, curr_time - self.last_time)
        self.last_time = curr_time


class Checkpointer(IterationHandler):

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
