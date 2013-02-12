import argparse
import os
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics, Perplexity
from pytopic.pipeline.preprocess import split_corpus

def convert_param(param):
    if param.isdigit():
        return int(param)
    elif param.isalpha():
        return param.lower() in ['true', 'yes']
    else:
        return float(param)


def get_cli_opts(dataset_name, args=None):
    desc = 'Run mixture of multinomials on the {} dataset'.format(dataset_name)
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('--inference', nargs='+', default=['gibbs'])
    parser.add_argument('--num-iters', type=int, default=1000)
    parser.add_argument('--print-interval', type=int, default=10)
    parser.add_argument('--train-percent', type=float, default=.8)
    opts = parser.parse_args(args)
    opts.inference[1:] = [convert_param(param) for param in opts.inference[1:]]
    return opts


def get_pssh_opts(dataset_name):
    pssh_opts = ['--inference gibbs',
                 '--inference em',
                 '--inference vem',
                 '--inference map']
    args = pssh_opts[int(os.environ['PSSH_NODENUM']) % len(pssh_opts)]
    return get_cli_opts(dataset_name, args.split())


def get_opts(dataset_name):
    if 'PSSH_NODENUM' in os.environ:
        return get_pssh_opts(dataset_name)
    else:
        return get_cli_opts(dataset_name)

def get_model(training, test, clustering, opts):
    K = len(clustering.labels)
    model = MixtureMultinomial(training, K, 2, .001)
    model.set_inference(*opts.inference)

    model.register_handler(Timer())
    model.register_handler(ClusterMetrics(clustering, opts.print_interval))
    model.register_handler(Perplexity(test, opts.print_interval))

    return model

def print_opts(opts):
    for key, value in vars(opts).iteritems():
        print '{}: {}'.format(key, value)

def main(dataset_name, corpus_func, clustering_func):
    opts = get_opts(dataset_name)
    print_opts(opts)

    training, test = split_corpus(corpus_func(), opts.train_percent)
    clustering = clustering_func(training)
    model = get_model(training, test, clustering, opts)

    model.inference(opts.num_iters)
