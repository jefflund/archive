from scripts.kdd13.corpora import get_newsgroups
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics
from pytopic.analysis.cluster import Clustering
from pytopic.pipeline.preprocess import split_corpus

def get_model(corpus):
    mm = MixtureMultinomial(corpus, 20, 2, .001)
    mm.set_inference('map')
    mm.register_handler(Timer())
    mm.register_handler(Printer(10))
    mm.register_handler(ClusterMetrics(Clustering.from_corpus(corpus), 10))
    return mm


if __name__ == '__main__':
    corpus = get_newsgroups()
    training, test = split_corpus(corpus, .5)
    mm = get_model(training)
    mm.inference(10)
    print mm.calc_perplexity(test)
