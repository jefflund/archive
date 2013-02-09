import argparse
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


def get_opts():
    parser = argparse.ArgumentParser(description='Runs MM on 20NG')
    parser.add_argument('--inference', nargs='+', default=['gibbs'])
    parser.add_argument('--num-iters', type=int, default=100)
    parser.add_argument('--print-interval', type=int, default=10)
    parser.add_argument('--train-percent', type=float, default=.5)
    opts = parser.parse_args()
    opts.inference[1:] = [convert_param(param) for param in opts.inference[1:]]
    return opts


def main():
    opts = get_opts()
    print opts
    corpus = get_newsgroups()
    model = get_model(corpus, opts)
    model.inference(opts.num_iters)


if __name__ == '__main__':
    main()
