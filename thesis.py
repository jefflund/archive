#!/usr/bin/env python

import os
import argparse

import evilplot


def time_metric_plot(results_dir, time_col, metric_col, **kwargs):
    plot = evilplot.LinePlot(**kwargs)
    for root, _, filenames in os.walk(results_dir):
        linename = os.path.basename(root)
        for filename in filenames:
            with open(os.path.join(root, filename)) as columns:
                plot.add_from_column(linename, columns, time_col, metric_col)
    return plot


def _mm_metric_plot(results_dir, dataset_name, col, metric):
    title = 'Mixture of Multinomials on {}'.format(dataset_name)
    plot = time_metric_plot(results_dir, 0, col, title=title,
                                                 xlabel='Time (seconds)',
                                                 ylabel=metric)
    plot.show(end_error=True)


def show_fmeasure_plot(results_dir, dataset_name):
    _mm_metric_plot(results_dir, dataset_name, 1, 'F-Measure')


def show_ari_plot(results_dir, dataset_name):
    _mm_metric_plot(results_dir, dataset_name, 2, 'Adjusted Rand Index')


def show_varinfo_plot(results_dir, dataset_name):
    _mm_metric_plot(results_dir, dataset_name, 3, 'Variation of Information')


def show_accuracy_plot(results_dir, dataset_name):
    title = 'Interactive Topic Model on {}'.format(dataset_name)
    plot = time_metric_plot(results_dir, 0, 1, title=title,
                                               xlabel='Time (seconds)',
                                               ylabel='Accuracy')
    plot.show(point_errors=True)


_METRIC_MAPPING = {'fm': show_fmeasure_plot,
                   'vi': show_varinfo_plot,
                   'ari': show_ari_plot}


def main():
    parser = argparse.ArgumentParser('Make plots for my thesis')
    parser.add_argument('result_dir')
    parser.add_argument('-m', '--metric', default='fm')
    args = parser.parse_args()

    dataset_name = 'Enron' if 'en' in args.result_dir else '20 Newsgroups'
    if 'mm' in args.result_dir:
        _METRIC_MAPPING[args.metric](args.result_dir, dataset_name)
    elif 'itm' in args.result_dir:
        show_accuracy_plot(args.result_dir, dataset_name)

if __name__ == '__main__':
    main()
