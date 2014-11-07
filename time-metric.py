#!/usr/bin/env python

import os
import argparse

import pylab
import evilplot
from evilplot import util

parser = argparse.ArgumentParser('Plot a metric versus time')
parser.add_argument('data')
parser.add_argument('-t', '--time-col', type=int, default=0)
parser.add_argument('-m', '--metric-col', type=int, default=1)
parser.add_argument('-mn', '--metric-name')
parser.add_argument('-T', '--title')
parser.add_argument('-o', '--output')
parser.add_argument('-e', '--error', choices=['end', 'point'])
parser.add_argument('--extra')
args = parser.parse_args()

plot = evilplot.LinePlot(title=args.title,
                         xlabel='Time (seconds)',
                         ylabel=args.metric_name)
util.add_extra_opts(plot, args.extra)
if args.error == 'end':
    plot.end_error = True
elif args.error == 'point':
    plot.point_errors = True

evilplot.crawl_results(plot, args.data, args.time_col, args.metric_col)

old_plot = plot.plot
def new_plot():
    old_plot()
    pylab.scatter([12.81], [.7905], 40, ['#e41a1c'], 'x')
    print 'foo'
plot.plot = new_plot

plot.xlim = (0, 100)

if args.output:
    if args.output.endswith('.tex'):
        plot.write_pgf(args.output[:-4])
    else:
        plot.save(args.output)
else:
    plot.show()
