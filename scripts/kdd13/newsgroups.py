#!/usr/bin/pypy

from pytopic.analysis.cluster import Clustering
from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache
from scripts.kdd13.runner import main

@pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_newsgroups():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/newsgroups.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


main('20 Newsgroups', get_newsgroups, Clustering.from_corpus)
