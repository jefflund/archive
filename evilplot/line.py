"""Provides LinePlot"""

from __future__ import division

import pylab

from evilplot.plot import Plot


class LinePlot(Plot):
    """A Plot with runs aggregated into lines"""

    def __init__(self, **kwargs):
        Plot.__init__(self, **kwargs)

    def pylab_plot(self):
        for name, line in self.iter_data():
            x, y = zip(*line)
            pylab.plot(x, y, label=name)

    def pgf_plot(self, prefix):
        for name, line in self.iter_data():
            with open(self._pgf_datname(prefix, name), 'w') as datfile:
                for x, y in line:
                    print >> datfile, x, y
