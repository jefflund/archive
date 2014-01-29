from __future__ import division

import pylab
import itertools


class PlotData(object):
    """Stores and aggregates plot data from multiple sources"""

    def __init__(self):
        self.data = []

    def add_xys(self, xys):
        """Adds a list of xy tuples to the data"""
        self.data.append(xys)

    def add_from_column(self, columns, col_x, col_y):
        """Adds a list of xy tuples from the column data"""
        xys = []
        for line in columns:
            comment = line.find('#')
            if comment >= 0:
                line = line[:comment]
            line = line.strip()
            if not line:
                continue

            line = line.split()
            xy = float(line[col_x]), float(line[col_y])
            xys.append(xy)
        self.add_xys(xys)

    def __getitem__(self, index):
        x, y = zip(*(l[index if len(l) > index else -1] for l in self.data))
        return pylab.mean(x), pylab.mean(y)

    def __len__(self):
        return max(len(line) for line in self.data)

    def __iter__(self):
        for i in range(len(self)):
            yield self[i]


class Plot(object):
    """Base class for plots"""

    def __init__(self, **kwargs):
        self.data = {}

        self.xlabel = kwargs.get('xlabel', 'X')
        self.ylabel = kwargs.get('ylabel', 'Y')
        self.legend = kwargs.get('legend', 'best')
        self.title = kwargs.get('title')

    def add_xys(self, name, xys):
        """Adds a list of xy tuples to the data for the given name"""
        self[name].add_xys(xys)

    def add_from_column(self, name, columns, col_x, col_y):
        """Adds a list of xy tuples from the column data for the given name"""
        self[name].add_from_column(columns, col_x, col_y)

    def plot(self):
        """Generates the plot for show or save"""
        raise NotImplementedError()

    def show(self):
        """Shows the plot"""
        self.plot()
        pylab.show()

    def save(self, filename):
        """Saves the plot"""
        self.plot()
        pylab.savefig(filename)

    def _show_opt(self, opt_name, kwname=None):
        opt_val = getattr(self, opt_name)
        if opt_val:
            opt_call = getattr(pylab, opt_name)
            if kwname:
                opt_call(**{kwname: opt_val})
            else:
                opt_call(opt_val)

    def __getitem__(self, name):
        if name not in self.data:
            self.data[name] = PlotData()
        return self.data[name]
