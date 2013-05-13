import argparse
from pytopic.analysis import classify
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

        fm = cluster.f_measure(contingency)
        ari = cluster.ari(contingency)
        vi = cluster.variation_info(contingency)

        plots['f-measure'].append_data(param, time, fm)
        plots['ari'].append_data(param, time, ari)
        plots['vi'].append_data(param, time, vi)

plots['f-measure'].show()
