"""A collection of utility functions for evilplot"""

import os


def crawl_results(plot, results_dir, x_col, y_col):
    """Adds results from the given directory to the Plot"""
    for root, _, filenames in os.walk(results_dir):
        linename = os.path.basename(root)
        for filename in filenames:
            with open(os.path.join(root, filename)) as columns:
                plot.add_from_column(linename, columns, x_col, y_col)


def add_extra_opts(plot, options):
    """Applies extra options from a string to a plot"""
    if options:
        for option in options.split(';'):
            key, val = option.split('=')
            setattr(plot, key, val)
