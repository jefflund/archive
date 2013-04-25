from pytopic.model import mixmulti
from pytopic.util import handler
from scripts.corpora import newsgroups

def get_model(corpus, clustering):
    model = mixmulti.MixtureMultinomial(corpus, 20, 2, .001)
    model.register_handler(handler.ClusterConvergeceCheck(model.k))
    model.register_handler(handler.StateTimePrinter(model.k))
    return model


if __name__ == '__main__':
    corpus = newsgroups.get_corpus()
    clustering = newsgroups.get_clustering(corpus)
    model = get_model(corpus, clustering)

    print 'gold', repr(clustering.data)
    model.unlimited_inference()
