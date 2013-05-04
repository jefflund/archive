#!/usr/bin/pypy

from pytopic.model import mixmulti
from pytopic.util import handler
from scripts.corpora import newsgroups

K = 20
GAMMA = 2
BETA = .001

RUN_TIME = 1200
ANNEAL_SCHEDULE = [25, 10, 5, 1]

def get_model(corpus, clustering):
    model = mixmulti.MixtureMultinomial(corpus, K, GAMMA, BETA)
    model.register_handler(handler.ClusterConvergeceCheck(model.k))
    model.register_handler(handler.StateTimePrinter(model.k))
    return model

def run(corpus, clustering, inference):
    model = get_model(corpus, clustering)
    print 'params', inference

    if inference.startswith('annealed'):
        for temp in ANNEAL_SCHEDULE:
            model.set_inference(inference, temp)
            model.timed_inference(RUN_TIME / len(ANNEAL_SCHEDULE))
    else:
        model.set_inference(inference)
        model.timed_inference(RUN_TIME)

if __name__ == '__main__':
    corpus = newsgroups.get_corpus()
    clustering = newsgroups.get_clustering(corpus)
    print 'gold', repr(clustering.data)

    run(corpus, clustering, 'ccm')
    run(corpus, clustering, 'em')
    run(corpus, clustering, 'vem')
    run(corpus, clustering, 'gibbs')

    run(corpus, clustering, 'annealed gibbs')
    run(corpus, clustering, 'annealed em')
    run(corpus, clustering, 'annealed vem')
