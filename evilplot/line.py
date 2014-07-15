"""Provides LinePlot"""

from __future__ import division

import pylab
import itertools

from evilplot.plot import Plot


def plot_line(line, label, color, style):
    """Plots a PlotData as a line with the given color and style"""
    x, y = zip(*line)
    pylab.plot(x, y, label=label, color=color, linestyle=style)


def plot_errors(line, color):
    """Plots a PlotData as error markers"""
    muxs, muys, errxs, errys = [], [], [], []
    for i in xrange(len(line)):
        x, y = zip(*(l[i if len(l) > i else -1] for l in line.data))
        muxs.append(pylab.mean(x))
        muys.append(pylab.mean(y))
        errxs.append(pylab.std(x))
        errys.append(pylab.std(y))
    pylab.errorbar(muxs, muys, errys, errxs, ecolor=color, label='_nolegend_')


def plot_end_error(line, color):
    """Plots the end point of a PlotData as an error marker"""
    x, y = zip(*(l[-1] for l in line.data))
    errx, erry = pylab.std(x), pylab.std(y)
    mux, muy = pylab.mean(x), pylab.mean(y)
    pylab.errorbar(mux, muy, erry, errx, ecolor=color, label='_nolegend_')


class LinePlot(Plot):
    """A Plot with runs aggregated into lines"""

    def __init__(self, **kwargs):
        Plot.__init__(self, **kwargs)

        self.color_cycle = ['#00305E', '#007722', '#990011']
        self.style_cycle = ['-', '--', ':', '-.']

        self.end_error = kwargs.get('end_error', False)
        self.point_errors = kwargs.get('point_errors', False)

    def plot(self):
        get_color = itertools.cycle(self.color_cycle).next
        get_style = itertools.cycle(self.style_cycle).next
        for name, line in self.iter_data():
            color, style = get_color(), get_style()
            plot_line(line, name, color, style)

            if self.point_errors:
                plot_errors(line, color)
            elif self.end_error:
                plot_end_error(line, color)

        self.default_opts()
