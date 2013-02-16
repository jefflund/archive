#!/usr/bin/pypy

from scripts.kdd13.runner import main
from scripts.kdd13.corpora import get_webkb
from pytopic.analysis.cluster import Clustering

main('WebKB', get_webkb, Clustering.from_corpus)
