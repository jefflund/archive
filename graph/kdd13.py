import argparse
from pytopic.analysis import cluster
from graph import plot

parser = argparse.ArgumentParser()
parser.add_argument('outfile')
args = parser.parse_args()

plotter = plot.Plotter()

gold = None
param = None

plots = {key: plot.Plotter() for key in ['f-measure', 'ari', 'vi']}

for line in open(args.outfile):
    time, data = line.split(None, 1)

    if time == 'gold':
        gold = cluster.Clustering.from_repr(data)
    elif time.startswith('param'):
        param = data
    else:
        pred = cluster.Clustering.from_repr(data)
        contingency = cluster.Contingency(gold, pred)

        plots['f-measure'].append_data(param, cluster.f_measure(contingency))
        plots['ari'].append_data(param, cluster.ari(contingency))
        plots['vi'].append_data(param, cluster.variation_info(contingency))

        for plot in plots.values():
            plot.append_data('time', float(time))

plots['f-measure'].show('time')
