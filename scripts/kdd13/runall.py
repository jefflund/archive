#!/usr/bin/python

import os
import argparse

pssh = 'pssh-run'
cmd = '"cd research/pytopic; pypy scripts/kdd13/{0}.py"'
name = '{0}{1}'
potatoes = '/config/potatoes/all'
run = ' '.join([pssh, cmd, name, potatoes])

parser = argparse.ArgumentParser()
parser.add_argument('pssh_runs', type=int, default=4, nargs='?') # 4=>100 runs
print parser.parse_args()
pssh_runs = parser.parse_args().pssh_runs

for i in range(pssh_runs):
    print run.format('newsgroups', i)
    print run.format('enron', i)
    #os.system(run.format('newsgroups', i))
    #os.system(run.format('enron', i))
