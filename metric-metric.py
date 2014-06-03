#!/usr/bin/env python

import argparse

import evilplot

parser = argparse.ArgumentParser('Plot a metric versus time')
parser.add_argument('data')
parser.add_argument('-x', '--x-col', type=int, default=0)
parser.add_argument('-xn', '--x-name')
parser.add_argument('-y', '--y-col', type=int, default=1)
parser.add_argument('-yn', '--y-name')
parser.add_argument('-T', '--title')
parser.add_argument('-o', '--output')
args = parser.parse_args()

plot = evilplot.ScatterPlot(title=args.title,
                            xlabel=args.x_name,
                            ylabel=args.y_name)

evilplot.crawl_results(plot, args.data, args.x_col, args.y_col)

if args.output:
    plot.save(args.output)
else:
    plot.show()
