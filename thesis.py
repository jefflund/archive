#!/usr/bin/env python

import os
import sys

import evilplot

def time_metric_plot(results_dir, time_col, metric_col, **kwargs):
    plot = evilplot.Plot(**kwargs)
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


if __name__ == '__main__':
    name = sys.argv[1]
    if 'mm' in name:
        if 'en' in name:
            show_fmeasure_plot(name, 'Enron')
        elif 'ng' in name:
            show_ari_plot(name, '20 Newsgroups')
    elif 'itm' in name:
        if 'en' in name:
            show_accuracy_plot(name, 'Enron')
        elif 'ng' in name:
            show_accuracy_plot(name, '20 Newsgroups')
