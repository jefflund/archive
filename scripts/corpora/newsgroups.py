from pytopic.pipeline import dataset, tokenizer, preprocess
from pytopic.analysis import cluster
from pytopic.util import data

@data.pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_corpus():
    reader = dataset.CorpusReader(tokenizer.NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = preprocess.load_stopwords('../data/stopwords/english.txt',
                                          '../data/stopwords/newsgroups.txt')
    corpus = preprocess.filter_stopwords(corpus, stopwords)

    return corpus

@data.pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_clustering(corpus):
    return cluster.Clustering.from_corpus(corpus)
