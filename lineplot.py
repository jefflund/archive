#!/usr/bin/env python
"""A simple script for quickly producing a line plot"""

import argparse

import evilplot
from evilplot import util

def main():
    """Runs the lineplot script"""
    parser = argparse.ArgumentParser('Creates a line plot')
    parser.add_argument('data')
    parser.add_argument('-x', '--x-col', type=int, default=0)
    parser.add_argument('-xn', '--x-name')
    parser.add_argument('-y', '--y-col', type=int, default=1)
    parser.add_argument('-yn', '--y-name')
    parser.add_argument('-s', '--show', type=bool, default=True)
    parser.add_argument('-o', '--output', type=str, default='')
    parser.add_argument('-p', '--pgf', type=str, default='')
    parser.add_argument('-X', '--extra', type=str, default='')
    args = parser.parse_args()

    plot = evilplot.LinePlot(xlabel=args.x_name, ylabel=args.y_name)
    util.add_extra_opts(plot, args.extra)
    evilplot.crawl_results(plot, args.data, args.x_col, args.y_col)

    if args.show:
        plot.show()
    if args.output:
        plot.save(args.output)
    if args.pgf:
        plot.write(args.pgf)

if __name__ == '__main__':
    main()
