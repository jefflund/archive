from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.analysis.cluster import Clustering
from pytopic.util.data import pickle_cache

@pickle_cache('pickle/newsgroups/corpus.pickle')
def get_corpus():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    stopwords.add('writes')
    stopwords.add('article')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

@pickle_cache('pickle/newsgroups/clustering.pickle')
def get_clustering(corpus):
    return Clustering.from_corpus(corpus)

if __name__ == '__main__':
    corpus = get_corpus()
    clustering = get_clustering()

