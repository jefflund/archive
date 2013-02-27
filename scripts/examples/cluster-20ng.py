from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import ClusterConvergeceCheck
from pytopic.analysis.cluster import Clustering, Contingency, f_measure, ari

@pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_corpus():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/newsgroups.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

@pickle_cache('../pickle/newsgroups-clustering.pickle')
def get_clustering(corpus):
    return Clustering.from_corpus(corpus)

def get_model(corpus):
    model = MixtureMultinomial(corpus, 20, 2, .001)
    model.set_inference('ecm')
    model.register_handler(ClusterConvergeceCheck(model.k))
    return model

def print_stuff(verbose=True):
    contingency = Contingency(clustering, Clustering.from_model(model))

    print 'Likelihood:', model.likelihood()
    print 'F-Measure:', f_measure(contingency)
    print 'ARI:', ari(contingency)
    print

    contingency.sort_contingency()
    contingency.print_contingency()
    print

    model.print_state(verbose=verbose)

if __name__ == '__main__':
    corpus = get_corpus()
    clustering = get_clustering(corpus)
    model = get_model(corpus)

    label_ids = {label: index for index, label in enumerate(clustering.labels)}
    for d, label in enumerate(clustering):
        model.set_k(d, label_ids[label])

    print_stuff(False)
    model.timed_inference(1200)
    print_stuff(True)
