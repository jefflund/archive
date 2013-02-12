#!/usr/bin/pypy

from scripts.kdd13.runner import main
from scripts.kdd13.corpora import get_newsgroups
from pytopic.analysis.cluster import Clustering

main('20 Newsgroups', get_newsgroups, Clustering.from_corpus)
