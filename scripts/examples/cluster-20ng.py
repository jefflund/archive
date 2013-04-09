from pytopic.pipeline import dataset, tokenizer, preprocess
from pytopic.util import data
from pytopic.model import mixmulti
from pytopic.analysis import cluster
from pytopic.util import handler

@data.pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_corpus():
    reader = dataset.CorpusReader(tokenizer.NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = preprocess.load_stopwords('../data/stopwords/english.txt',
                                          '../data/stopwords/newsgroups.txt')
    corpus = preprocess.filter_stopwords(corpus, stopwords)

    return corpus

@data.pickle_cache('../pickle/newsgroups-clustering.pickle')
def get_clustering(corpus):
    return cluster.Clustering.from_corpus(corpus)

def get_model(corpus, clustering):
    model = mixmulti.MixtureMultinomial(corpus, 20, 2, .001)
    model.register_handler(handler.Timer())
    model.register_handler(handler.ClusterConvergeceCheck(model.k))
    model.register_handler(handler.MetricPrinter(5, clustering))
    return model

if __name__ == '__main__':
    corpus = get_corpus()
    clustering = get_clustering(corpus)
    model = get_model(corpus, clustering)
    model.unlimited_inference()
