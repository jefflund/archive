#!/usr/bin/env python

import argparse
import os
import pylab

import evilplot
from evilplot import util
from evilplot.plot import Plot


class CleanPlot(Plot):

    def __init__(self, **kwargs):
        Plot.__init__(self, **kwargs)

        #self.color_cycle = ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3']
        #self.marker_cycle = ['o', 's', 'D', 'v', '^', '>', '<', 'x', '+']

        self.use_ends = kwargs.get('use_ends', False)

    def plot(self):
        #get_color = itertools.cycle(self.color_cycle).next
        #get_marker = itertools.cycle(self.marker_cycle).next

        names = list(self.data)
        first, second = names[0], names[1]
        for fpts, spts in zip(self.data[first], self.data[second])[:100]:
            fx, fy = fpts
            sx, sy = spts
            pylab.plot([fx, sx], [fy, sy])


        self.default_opts()


parser = argparse.ArgumentParser('Plot a metric versus metric')
parser.add_argument('data')
parser.add_argument('-xn', '--x-name')
parser.add_argument('-x1', '--x1-col', type=int, default=0)
parser.add_argument('-x2', '--x2-col', type=int, default=1)
parser.add_argument('-yn', '--y-name')
parser.add_argument('-y1', '--y1-col', type=int, default=2)
parser.add_argument('-y2', '--y2-col', type=int, default=3)
parser.add_argument('-n1', '--name-1', default='1')
parser.add_argument('-n2', '--name-2', default='2')
parser.add_argument('-T', '--title')
parser.add_argument('-o', '--output')
parser.add_argument('--extra')
parser.add_argument('--use-last', action='store_true', default=False)
args = parser.parse_args()

plot = CleanPlot(title=args.title,
                 xlabel=args.x_name,
                 ylabel=args.y_name)
util.add_extra_opts(plot, args.extra)

for root, _, filenames in os.walk(args.data):
    for filename in filenames:
        if args.use_last:
            with open(os.path.join(root, filename)) as columns:
                plot.add_last_from_column(args.name_1, columns, args.x1_col, args.y1_col)
            with open(os.path.join(root, filename)) as columns:
                plot.add_last_from_column(args.name_2, columns, args.x2_col, args.y2_col)
        else:
            with open(os.path.join(root, filename)) as columns:
                plot.add_from_column(args.name_1, columns, args.x1_col, args.y1_col)
            with open(os.path.join(root, filename)) as columns:
                plot.add_from_column(args.name_2, columns, args.x2_col, args.y2_col)

if args.output:
    plot.save(args.output)
else:
    plot.show()
