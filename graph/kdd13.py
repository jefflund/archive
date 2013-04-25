import argparse
from pytopic.analysis import cluster
from graph import plot

parser = argparse.ArgumentParser()
parser.add_argument('outfile')
args = parser.parse_args()

plotter = plot.Plotter()

gold = None

for line in open(args.outfile):
    time, data = line.split(None, 1)

    if time == 'gold':
        gold = cluster.Clustering.from_repr(data)
    else:
        pred = cluster.Clustering.from_repr(data)
        contingency = cluster.Contingency(gold, pred)

        plotter.append_data('f-measure', cluster.f_measure(contingency))
        plotter.append_data('ari', cluster.ari(contingency))
        plotter.append_data('vi', cluster.variation_info(contingency))
        plotter.append_data('time', float(time))

plotter.show('time', ykey='f-measure')
