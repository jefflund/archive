#!/usr/bin/pypy

from pytopic.model import vanilla
from pytopic.util import handler
from scripts.corpora import newsgroups

T = 20
ALPHA= .4
BETA = .01

RUN_TIME = 1200
ANNEAL_SCHEDULE = [25, 10, 5, 1]

def get_model(corpus):
    model = vanilla.VanillaLDA(corpus, T, ALPHA, BETA)
    model.register_handler(handler.TopicConvergenceCheck(model.z))
    model.register_handler(handler.StateTimePrinter(model.z))
    return model

def run(corpus, inference):
    model = get_model(corpus) 
    print 'params', inference
    model.set_inference(inference)
    model.timed_inference(RUN_TIME)

if __name__ == '__main__':
    corpus = newsgroups.get_corpus()

    run(corpus, 'ccm')
    run(corpus, 'gibbs')
