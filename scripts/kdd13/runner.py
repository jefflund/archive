import argparse
import os
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics, Perplexity
from pytopic.pipeline.preprocess import split_corpus

def get_cli_opts(dataset_name, args=None):
    desc = 'Run mixture of multinomials on the {} dataset'.format(dataset_name)
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('--inference', action='store', default='gibbs')
    parser.add_argument('--num-iters', type=int, default=100)
    parser.add_argument('--print-interval', type=int, default=1)
    parser.add_argument('--train-percent', type=float, default=.8)
    parser.add_argument('--anneal', action='store_true', default=False)
    opts = parser.parse_args(args)
    return opts


def get_pssh_opts(dataset_name):
    pssh_opts = [['--inference', 'gibbs'],
                 ['--inference', 'gibbs', '--anneal'],
                 ['--inference', 'map'],
                 ['--inference', 'map', '--anneal'],
                 ['--inference', 'em'],
                 ['--inference', 'em', '--anneal'],
                 ['--inference', 'vem'],
                 ['--inference', 'vem', '--anneal']]
    args = pssh_opts[int(os.environ['PSSH_NODENUM']) % len(pssh_opts)]
    return get_cli_opts(dataset_name, args)


def get_opts(dataset_name):
    if 'PSSH_NODENUM' in os.environ:
        return get_pssh_opts(dataset_name)
    else:
        return get_cli_opts(dataset_name)


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


def main(dataset_name, corpus_func, clustering_func):
    opts = get_opts(dataset_name)
    print_opts(opts)

    training, test = split_corpus(corpus_func(), opts.train_percent)
    clustering = clustering_func(training)
    model = get_model(training, test, clustering, opts)

    if opts.anneal:
        opts.inference = 'annealed {}'.format(opts.inference)
        model.set_inference(opts.inference, 25)
        model.inference(opts.num_iters)
        model.set_inference(opts.inference, 10)
        model.inference(opts.num_iters)
        model.set_inference(opts.inference, 5)
        model.inference(opts.num_iters)
        model.set_inference(opts.inference, 1)
        model.inference(opts.num_iters)
    else:
        model.set_inference(opts.inference)
        model.inference(opts.num_iters)
