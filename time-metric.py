#!/usr/bin/env python

import os
import argparse

import evilplot

parser = argparse.ArgumentParser('Plot a metric versus time')
parser.add_argument('data')
parser.add_argument('-t', '--time-col', type=int, default=0)
parser.add_argument('-m', '--metric-col', type=int, default=1)
parser.add_argument('-mn', '--metric-name')
parser.add_argument('-T', '--title')
parser.add_argument('-o', '--output')
parser.add_argument('-e', '--error', choices=['end', 'point'])
args = parser.parse_args()

plot = evilplot.LinePlot(title=args.title,
                         xlabel='Time (seconds)',
                         ylabel=args.metric_name)
if args.error == 'end':
    plot.end_error = True
elif args.error == 'point':
    plot.point_errors = True

evilplot.crawl_results(plot, args.data, args.time_col, args.metric_col)

if args.output:
    plot.save(args.output)
else:
    plot.show()
