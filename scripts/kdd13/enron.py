#!/usr/bin/pypy

from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache
from pytopic.analysis.cluster import Clustering
from scripts.kdd13.runner import main

ENRON_DATA = '../data/enron'
ENRON_INDEX = '../data/enron/indices/ldc_split/all'

@pickle_cache('../pickle/enron-corpus.pickle')
def get_enron():
    reader = CorpusReader(NewsTokenizer())
    reader.add_index_dir(ENRON_INDEX, ENRON_DATA)
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

def enron_clustering(corpus):
    return Clustering.from_indices(corpus, ENRON_INDEX, ENRON_DATA)

if __name__ == '__main__':
    main('Enron', get_enron, enron_clustering)
