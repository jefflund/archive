#!/usr/bin/pypy

import os
from scripts.kdd13.runner import main
from scripts.kdd13.corpora import get_webkb
from pytopic.analysis.cluster import Clustering

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
        if cluster in title:
            return cluster

def webkb_clustering(corpus):
    data =  [find_cluster(title) for title in corpus.titles]
    return Clustering(WEBKB_CLUSTERS, data)

main('WebKB', get_webkb, webkb_clustering)
