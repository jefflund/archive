#!/usr/bin/pypy

import os
from pytopic.analysis.cluster import Clustering
from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.pipeline.tokenizer import HTMLTokenizer
from pytopic.util.data import pickle_cache
from scripts.kdd13.runner import main


class WebKBTokenizer(HTMLTokenizer):

    def __init__(self, split_re='\s+', filter_re='[^a-zA-Z]'):
        HTMLTokenizer.__init__(self, split_re, filter_re)

    def tokenize(self, filename, buff):
        line = buff.readline()
        while line.strip() != '':
            line = buff.readline()
        return HTMLTokenizer.tokenize(self, filename, buff)


@pickle_cache('../pickle/webkb-corpus.pickle')
def get_webkb():
    reader = CorpusReader(WebKBTokenizer())
    reader.add_dir('../data/webkb')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


WEBKB_CLUSTERS = ['course',
                  'department',
                  'faculty',
                  'other',
                  'project',
                  'staff',
                  'student']

def find_cluster(title):
    for cluster in WEBKB_CLUSTERS:
        if 'webkb' + os.sep + cluster in title:
            return cluster

def webkb_clustering(corpus):
    data =  [find_cluster(title) for title in corpus.titles]
    return Clustering(WEBKB_CLUSTERS, data)


main('WebKB', get_webkb, webkb_clustering)
