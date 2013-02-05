from scripts.kdd13.corpora import get_newsgroups
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics
from pytopic.analysis.cluster import Clustering

def get_model(corpus):
    mm = MixtureMultinomial(corpus, 20, 2, 2)
    mm.set_inference('vem')
    mm.register_handler(Timer())
    mm.register_handler(Printer(10))
    mm.register_handler(ClusterMetrics(Clustering.from_corpus(corpus), 10))
    return mm

if __name__ == '__main__':
    corpus = get_newsgroups()
    mm = get_model(corpus)
    mm.inference(100)
