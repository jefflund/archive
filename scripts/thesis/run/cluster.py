#!/usr/bin/pypy

import argparse
from pytopic.model import mixmulti
from pytopic.util import handler
from scripts.corpora import newsgroups

RUN_TIME = 1200
ANNEAL_SCHEDULE = [25, 10, 5, 1]
INFERENCE_ALGORITHMS = ['ccm', 'gibbs', 'vem', 'em',
                        'annealed gibbs', 'annealed vem', 'annealed em']

OPTS = {'20ng': tuple()}

def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--corpus', default='20ng',
                        help='Corpus to use ({})'.format(', '.join(OPTS)))
    return parser.parse_args()

def get_model(corpus, clustering, K, gamma, beta):
    model = mixmulti.MixtureMultinomial(corpus, K, gamma, beta)
    model.register_handler(handler.ClusterConvergeceCheck(model.k))
    model.register_handler(handler.StateTimePrinter(model.k))
    return model

def run(corpus, clustering, inference):
    model = get_model(corpus, clustering, 20, 2, .001)
    print 'params', inference

    if inference.startswith('annealed'):
        for temp in ANNEAL_SCHEDULE:
            model.set_inference(inference, temp)
            model.timed_inference(RUN_TIME / len(ANNEAL_SCHEDULE))
    else:
        model.set_inference(inference)
        model.timed_inference(RUN_TIME)

if __name__ == '__main__':
    args = get_args()
    opts = OPTS[args.corpus]

    corpus = newsgroups.get_corpus()
    clustering = newsgroups.get_clustering(corpus)
    print 'gold', repr(clustering.data)

    for algorithm in INFERENCE_ALGORITHMS:
        run(corpus, clustering, algorithm)
