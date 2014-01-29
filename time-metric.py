#!/usr/bin/env python

import os
import argparse

import evilplot


def time_metric_plot(results_dir, time_col, metric_col):
    plot = evilplot.LinePlot()
    for root, _, filenames in os.walk(results_dir):
        linename = os.path.basename(root)
        for filename in filenames:
            with open(os.path.join(root, filename)) as columns:
                plot.add_from_column(linename, columns, time_col, metric_col)
    return plot


def main():
    parser = argparse.ArgumentParser('Plot a metric versus time')
    parser.add_argument('results_dir')
    parser.add_argument('-tc', '--time-col', type=int, default=0)
    parser.add_argument('-mc', '--metric-col', type=int, default=1)
    parser.add_argument('-t', '--title')
    parser.add_argument('-mn', '--metric-name')
    parser.add_argument('-o', '--output')
    parser.add_argument('-e', '--error', choices=['end', 'point'])
    args = parser.parse_args()

    plot = time_metric_plot(args.results_dir, args.time_col, args.metric_col)
    plot.title = args.title
    plot.xlabel = 'Time (seconds)'
    plot.ylabel = args.metric_name
    if args.error == 'end':
        plot.end_error = True
    elif args.error == 'point':
        plot.point_errors = True

    if args.output:
        plot.save(args.output)
    else:
        plot.show()

if __name__ == '__main__':
    main()
