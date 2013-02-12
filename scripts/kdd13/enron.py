#!/usr/bin/pypy

from scripts.kdd13.runner import main
from scripts.kdd13.corpora import get_enron
from pytopic.analysis.cluster import Clustering

main('Enron', get_enron, Clustering.from_corpus)
