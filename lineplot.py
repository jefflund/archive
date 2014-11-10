#!/usr/bin/env python

import os
import argparse

import pylab
import evilplot

parser = argparse.ArgumentParser('Creates a line plot')
parser.add_argument('data')
parser.add_argument('-x', '--x-col', type=int, default=0)
parser.add_argument('-xn', '--x-name')
parser.add_argument('-y', '--y-col', type=int, default=1)
parser.add_argument('-yn', '--y-name')
parser.add_argument('-s', '--show', type=bool, default=True)
parser.add_argument('-o', '--output', type=bool, default=False)
parser.add_argument('-p', '--pgf', type=bool, default=True)
args = parser.parse_args()

plot = evilplot.LinePlot(xlabel=args.x_name, ylabel=args.y_name)

evilplot.crawl_results(plot, args.data, args.x_col, args.y_col)

if args.show:
    plot.show()
if args.output:
    plot.save(args.output)
if args.pgf:
    plot.write(args.pgf)
