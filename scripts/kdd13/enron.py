#!/usr/bin/pypy

from scripts.kdd13.runner import main
from scripts.kdd13.corpora import get_enron
from pytopic.analysis.cluster import Clustering

def enron_clustering(corpus):
    return Clustering.from_indices(corpus, 
                                   '../data/enron/indices/ldc_split/all',
                                   '../data/enron')

main('Enron', get_enron, enron_clustering)
