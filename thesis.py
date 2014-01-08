import os

import evilplot

def time_metric_plot(results_dir, time_col, metric_col, **kwargs):
    plot = evilplot.Plot(**kwargs)
    for root, _, filenames in os.walk(results_dir):
        linename = os.path.basename(root)
        for filename in filenames:
            with open(os.path.join(root, filename)) as columns:
                plot.add_from_column(linename, columns, time_col, metric_col)
    return plot


def show_fmeasure_plot(results_dir, dataset_name):
    title = 'Mixture of Multinomials on {}'.format(dataset_name)
    plot = time_metric_plot(results_dir, 0, 1, title=title,
                                               xlabel='Time (seconds)',
                                               ylabel='F-Measure')
    plot.show(end_error=True)
