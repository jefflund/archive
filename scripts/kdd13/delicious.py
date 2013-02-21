#!/usr/bin/pypy

from pytopic.analysis.cluster import Clustering
from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import HTMLTokenizer
from pytopic.util.data import pickle_cache
from pytopic.pipeline.preprocess import (load_stopwords, filter_stopwords,
                                         filter_rarewords)
from scripts.kdd13.runner import main

@pickle_cache('../pickle/delicious-corpus.pickle')
def get_delicious():
    reader = CorpusReader(HTMLTokenizer())
    reader.add_dir('../data/delicious')
    corpus = reader.read()

    corpus = filter_rarewords(corpus, 10)
    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/web.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


if __name__ == '__main__':
    main('Delicious', get_delicious, Clustering.from_corpus)
