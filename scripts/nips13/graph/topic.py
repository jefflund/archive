import argparse
from pytopic.analysis import classify
from graph import plot

parser = argparse.ArgumentParser()
parser.add_argument('outfile')
args = parser.parse_args()

labels = None
param = None


for line in open(args.outfile):
    time, data = line.split(None, 1)

    if time == 'gold':
        labels = cluster.Clustering.from_repr(data)
    elif time.startswith('param'):
        param = data
        print param
    else:
        print classify.cross_fold_validation(labels, eval(data))
