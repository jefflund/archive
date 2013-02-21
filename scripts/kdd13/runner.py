import argparse
import gc
import os
import random
import time
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.pipeline.preprocess import split_corpus
from pytopic.util.handler import Timer, ClusterConvergeceCheck, MetricPrinter

def get_opts(dataset_name, args=None):
    desc = 'Run mixture of multinomials on the {} dataset'.format(dataset_name)
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('--inference', action='store', default='gibbs')
    parser.add_argument('--run-time', type=int, default=1200)
    parser.add_argument('--print-interval', type=int, default=1)
    parser.add_argument('--train-percent', type=float, default=.8)
    parser.add_argument('--anneal', action='store_true', default=False)
    parser.add_argument('--seed', type=int, default=None)
    parser.add_argument('--random-restart', action='store_true', default=False)
    opts = parser.parse_args(args)
    return opts


def get_model(training, test, clustering, opts):
    K = len(clustering.labels)
    model = MixtureMultinomial(training, K, 2, .001)

    model.register_handler(Timer())
    model.register_handler(MetricPrinter(clustering, test))
    model.register_handler(ClusterConvergeceCheck(model.k))

    return model


def print_opts(opts):
    header = ['inference:']
    if opts.anneal:
        header.append('annealed')
    if opts.random_restart:
        header.append('random restart')
    header.append(opts.inference)
    print ' '.join(header)
    print 'seed:', opts.seed


def main(dataset_name, corpus_func, clustering_func):
    if 'PSSH_NODENUM' in os.environ:
        pssh_main(dataset_name, corpus_func, clustering_func)
    else:
        opts = get_opts(dataset_name)
        run(opts, corpus_func, clustering_func)


def pssh_main(dataset_name, corpus_func, clustering_func):
    seed = str(int(random.getrandbits(32)))
    pssh_opts = [['gibbs'],
                 ['ecm'],
                 ['em'],
                 ['vem'],
                 ['gibbs', '--anneal'],
                 ['em', '--anneal'],
                 ['vem', '--anneal'],
                 ['ecm', '--random-restart'],
                 ['em', '--random-restart'],
                 ['vem', '--random-restart'],
                 ['em', '--anneal', '--random-restart'],
                 ['vem', '--anneal', '--random-restart']]
    pssh_opts = [['--inference'] + opt + ['--seed', seed] for opt in pssh_opts]

    # so I can graph as data comes in from pssh
    node_num = int(os.environ['PSSH_NODENUM'])
    run_num = node_num % len(pssh_opts)
    pssh_opts = pssh_opts[run_num:] + pssh_opts[:run_num]

    for opts in pssh_opts:
        opts = get_opts(dataset_name, opts)
        run(opts, corpus_func, clustering_func)
        gc.collect()


def run(opts, corpus_func, clustering_func):
    print_opts(opts)
    random.seed(opts.seed)

    training, test = split_corpus(corpus_func(), opts.train_percent)
    clustering = clustering_func(training)
    gc.collect()
    model = get_model(training, test, clustering, opts)

    if opts.anneal:
        opts.inference = 'annealed {}'.format(opts.inference)

    end_time = time.time() + opts.run_time

    while time.time() < end_time:
        time_left = end_time - time.time()
        if opts.anneal:
            run_time = time_left / 4

            model.set_inference(opts.inference, 25)
            model.timed_inference(run_time)

            model.set_inference(opts.inference, 10)
            model.timed_inference(run_time)

            model.set_inference(opts.inference, 5)
            model.timed_inference(run_time)

            model.set_inference(opts.inference, 1)
            model.timed_inference(run_time)
        else:
            model.set_inference(opts.inference)
            model.timed_inference(time_left)

        if opts.random_restart:
            model.random_restart()
        else:
            break
