"""Provides LinePlot"""

from __future__ import division

import pylab
import itertools

from evilplot.plot import Plot


class LinePlot(Plot):
    """A Plot with runs aggregated into lines"""

    def __init__(self, **kwargs):
        Plot.__init__(self, **kwargs)

    def pylab_plot(self):
        for name, line in self.iter_data():
            x, y = zip(*line)
            plot_line(line, name, color, style)
            pylab.plot(x, y, label=name)
