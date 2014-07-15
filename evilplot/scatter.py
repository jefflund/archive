"""Provides ScatterPlot"""

from __future__ import division

import pylab
import itertools

from evilplot.plot import Plot


class ScatterPlot(Plot):
    """A Plot with runs displayed as points"""

    def __init__(self, **kwargs):
        Plot.__init__(self, **kwargs)

        self.color_cycle = ['#00305E', '#007722', '#990011']
        self.marker_cycle = ['o', '^', 'v', 's', 'p', '+', 'x', '*']

        self.use_ends = kwargs.get('use_ends', False)

    def plot(self):
        get_color = itertools.cycle(self.color_cycle).next
        get_marker = itertools.cycle(self.marker_cycle).next
        for name, scatter in self.iter_data():
            color, marker = get_color(), get_marker()
            if self.use_ends:
                scatter = [run[-1] for run in scatter.data]
            x, y = zip(*scatter)
            pylab.scatter(x, y, c=color, marker=marker, label=name)

        self.default_opts()
