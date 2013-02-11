#!/usr/bin/pypy

import argparse
import os
from scripts.kdd13.corpora import get_newsgroups
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics, Perplexity
from pytopic.analysis.cluster import Clustering
from pytopic.pipeline.preprocess import split_corpus

def get_model(corpus, opts):
    training, test = split_corpus(corpus, opts.train_percent)
    clustering = Clustering.from_corpus(training)

    mm = MixtureMultinomial(training, 20, 2, .001)
    mm.set_inference(*opts.inference)

    mm.register_handler(Timer())
    mm.register_handler(Printer(opts.print_interval))
    mm.register_handler(ClusterMetrics(clustering, opts.print_interval))
    mm.register_handler(Perplexity(test, opts.print_interval))

    return mm


def convert_param(param):
    if param.isdigit():
        return int(param)
    elif param.isalpha():
        return param.lower() in ['true', 'yes']
    else:
        return float(param)


def get_cli_opts(args=None):
    parser = argparse.ArgumentParser(description='Runs MM on 20NG')
    parser.add_argument('--inference', nargs='+', default=['gibbs'])
    parser.add_argument('--num-iters', type=int, default=1000)
    parser.add_argument('--print-interval', type=int, default=10)
    parser.add_argument('--train-percent', type=float, default=.8)
    opts = parser.parse_args(args)
    opts.inference[1:] = [convert_param(param) for param in opts.inference[1:]]
    return opts


def get_pssh_opts():
    pssh_opts = ['--inference gibbs',
                 '--inference em',
                 '--inference vem',
                 '--inference map']
    args = pssh_opts[int(os.environ['PSSH_NODENUM']) % len(pssh_opts)]
    return get_cli_opts(args.split())


def get_opts():
    if 'PSSH_NODENUM' in os.environ:
        return get_pssh_opts()
    else:
        return get_cli_opts()


def main():
    opts = get_opts()
    print opts
    corpus = get_newsgroups()
    model = get_model(corpus, opts)
    model.inference(opts.num_iters)


if __name__ == '__main__':
    main()
