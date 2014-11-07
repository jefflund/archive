#!/usr/bin/env python

import argparse

import evilplot
from evilplot import util

parser = argparse.ArgumentParser('Plot a metric versus metric')
parser.add_argument('data')
parser.add_argument('-x', '--x-col', type=int, default=0)
parser.add_argument('-xn', '--x-name')
parser.add_argument('-y', '--y-col', type=int, default=1)
parser.add_argument('-yn', '--y-name')
parser.add_argument('-T', '--title')
parser.add_argument('-o', '--output')
parser.add_argument('--extra')
parser.add_argument('--use-last', action='store_true', default=False)
args = parser.parse_args()

plot = evilplot.ScatterPlot(title=args.title,
                            xlabel=args.x_name,
                            ylabel=args.y_name)
util.add_extra_opts(plot, args.extra)

evilplot.crawl_results(plot, args.data, args.x_col, args.y_col, args.use_last)

if args.output:
    if output.endswith('.tex'):
        plot.write_pgf(args.output[:-4])
    else:
        plot.save(args.output)
else:
    plot.show()
