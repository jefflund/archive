import argparse
import gc
import os
import random
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics, Perplexity
from pytopic.pipeline.preprocess import split_corpus

def get_opts(dataset_name, args=None):
    desc = 'Run mixture of multinomials on the {} dataset'.format(dataset_name)
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('--inference', action='store', default='gibbs')
    parser.add_argument('--run-time', type=int, default=1200)
    parser.add_argument('--print-interval', type=int, default=1)
    parser.add_argument('--train-percent', type=float, default=.8)
    parser.add_argument('--anneal', action='store_true', default=False)
    parser.add_argument('--seed', type=int, default=None)
    opts = parser.parse_args(args)
    return opts


def get_model(training, test, clustering, opts):
    K = len(clustering.labels)
    model = MixtureMultinomial(training, K, 2, .001)

    model.register_handler(Timer())
    model.register_handler(ClusterMetrics(clustering, opts.print_interval))
    model.register_handler(Perplexity(test, opts.print_interval))

    return model


def print_opts(opts):
    header = 'inference: '
    if opts.anneal:
        header += 'annealed '
    header += opts.inference
    print header
    print 'seed:', opts.seed


def main(dataset_name, corpus_func, clustering_func):
    if 'PSSH_NODENUM' in os.environ:
        pssh_main(dataset_name, corpus_func, clustering_func)
    else:
        opts = get_opts(dataset_name)
        run(opts, corpus_func, clustering_func)


def pssh_main(dataset_name, corpus_func, clustering_func):
    seed = str(int(random.getrandbits(32)))
    pssh_opts = [['--inference', 'gibbs', '--seed', seed],
                 ['--inference', 'map', '--seed', seed],
                 ['--inference', 'em', '--seed', seed],
                 ['--inference', 'vem', '--seed', seed],
                 ['--anneal', '--inference', 'gibbs', '--seed', seed],
                 ['--anneal', '--inference', 'map', '--seed', seed],
                 ['--anneal', '--inference', 'em', '--seed', seed],
                 ['--anneal', '--inference', 'vem', '--seed', seed]]

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
    model = get_model(training, test, clustering, opts)

    if opts.anneal:
        opts.inference = 'annealed {}'.format(opts.inference)
        opts.run_time //= 4
        model.set_inference(opts.inference, 25)
        model.timed_inference(opts.run_time)
        model.set_inference(opts.inference, 10)
        model.timed_inference(opts.run_time)
        model.set_inference(opts.inference, 5)
        model.timed_inference(opts.run_time)
        model.set_inference(opts.inference, 1)
        model.timed_inference(opts.run_time)
    else:
        model.set_inference(opts.inference)
        model.timed_inference(opts.run_time)
