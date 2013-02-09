from scripts.kdd13.corpora import get_newsgroups
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics, Perplexity
from pytopic.analysis.cluster import Clustering
from pytopic.pipeline.preprocess import split_corpus

def get_model(corpus, iter_interval):
    training, test = split_corpus(corpus, .5)
    clustering = Clustering.from_corpus(training)
    mm = MixtureMultinomial(training, 20, 2, .001)
    mm.set_inference('gibbs')
    mm.register_handler(Timer())
    mm.register_handler(Printer(iter_interval))
    mm.register_handler(ClusterMetrics(clustering, iter_interval))
    mm.register_handler(Perplexity(test, iter_interval))
    return mm


if __name__ == '__main__':
    corpus = get_newsgroups()
    mm = get_model(corpus, 10)
    mm.inference(100)
