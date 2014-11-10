"""Provides ScatterPlot"""

from __future__ import division

import pylab

from evilplot.plot import Plot


class ScatterPlot(Plot):
    """A Plot with runs displayed as points"""

    def __init__(self, **kwargs):
        Plot.__init__(self, **kwargs)

    def pylab_plot(self):
        for name, scatter in self.iter_data():
            x, y = zip(*scatter)
            pylab.scatter(x, y, label=name)
