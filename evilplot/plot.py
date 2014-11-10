"""Provides Plot, as a base for other plots"""

from __future__ import division

import pylab


class PlotData(object):
    """Stores and aggregates plot data from multiple sources"""

    def __init__(self):
        self.data = []

    def add_xys(self, xys):
        """Adds a list of xy tuples to the data"""
        self.data.append(xys)

    @staticmethod
    def _get_xys_from_column(columns, col_x, col_y):
        xys = []
        for line in columns:
            comment = line.find('#')
            if comment >= 0:
                line = line[:comment]
            line = line.strip()
            if not line:
                continue

            line = line.split()
            xys.append((float(line[col_x]), float(line[col_y])))
        return xys

    def add_from_column(self, columns, col_x, col_y):
        """Adds a list of xy tuples from the column data"""
        xys = self._get_xys_from_column(columns, col_x, col_y)
        self.add_xys(xys)

    def add_last_from_column(self, columns, col_x, col_y):
        """Adds the last points from xy tuples from the column data"""
        xys = self._get_xys_from_column(columns, col_x, col_y)
        self.add_xys(xys[-1:])

    def __getitem__(self, index):
        x, y = zip(*(l[index if len(l) > index else -1] for l in self.data))
        return sum(x) / len(x), sum(y) / len(y)

    def __len__(self):
        return max(len(line) for line in self.data)

    def __iter__(self):
        for i in range(len(self)):
            yield self[i]


class Plot(object):
    """Base class for plots"""

    def __init__(self, **kwargs):
        self.data = {}

        self._xmin = kwargs.get('xmin', None)
        self._xmax = kwargs.get('xmax', None)
        self._ymin = kwargs.get('ymin', None)
        self._ymax = kwargs.get('ymax', None)

        self.xlabel = kwargs.get('xlabel', 'X')
        self.ylabel = kwargs.get('ylabel', 'Y')

        self.legend = kwargs.get('legend')

    def add_xys(self, name, xys):
        """Adds a list of xy tuples to the data for the given name"""
        self[name].add_xys(xys)

    def add_from_column(self, name, columns, col_x, col_y):
        """Adds a list of xy tuples from the column data for the given name"""
        self[name].add_from_column(columns, col_x, col_y)

    def add_last_from_column(self, name, columns, col_x, col_y):
        """Adds the last points from column data for the given name"""
        self[name].add_last_from_column(columns, col_x, col_y)

    def show(self):
        """Uses pylab to show the plot"""
        self._pylab_opts()
        self.pylab_plot()
        pylab.show()

    def save(self, filename):
        """Uses pylab to save the plot"""
        self._pylab_opts()
        self.pylab_plot()
        pylab.savefig(filename)

    def _pylab_opts(self):
        """Sets the plot options for pylab"""
        pylab.xlabel(self.xlabel)
        pylab.ylabel(self.ylabel)

        pylab.xlim(self.xmin, self.xmax)
        pylab.ylim(self.ymin, self.ymax)

        if self._need_legend():
            pylab.legend(loc=self.legend)

    def pylab_plot(self):
        """Plots the data using pylab"""
        raise NotImplementedError()

    def write(self, prefix):
        """Uses pgf plots to Write the plot as a tikzpicture"""
        with open(prefix + '.tex', 'w') as texfile:
            self._write_pgf_opts(texfile, prefix)
            self.pgf_plot(prefix)

    def _write_pgf_opts(self, texfile, prefix):
        print >> texfile, r'\begin{tikzpicture}'
        print >> texfile, r'\begin{axis}{'

        print >> texfile, r'    xlabel={\small %s},' % self.xlabel
        print >> texfile, r'    ylabel={\small %s},' % self.ylabel

        print >> texfile, r'    xmin=%d,' % self.xmin
        print >> texfile, r'    xmax=%d,' % self.xmax
        print >> texfile, r'    ymin=%d,' % self.ymin
        print >> texfile, r'    ymax=%d,' % self.ymax

        if self._need_legend():
            print >> texfile, r'    legend pos=%s' % self.legend
            print >> texfile, r'    legend cell align=left,',

        print >> texfile, r'    xlabel near ticks,'
        print >> texfile, r'    ylabel near ticks,'

        print >> texfile, r']'
        print >> texfile

        for name, _ in self.iter_data():
            plot = r'\addplot +[mark=none] table[x index=0,y index=1] {%s};'
            print >> texfile, plot % self._pgf_datname(prefix, name)
            if self._need_legend():
                print >> texfile, r'\addlegendentry{\small %s}' % name

        print >> texfile, r'\end{axis}'
        print >> texfile, r'\end{tikzpicture}'

    @staticmethod
    def _pgf_datname(prefix, name):
        return prefix + '-' + name + '.dat'

    def pgf_plot(self, prefix):
        """Writes the plot data to a texfile and dat files"""
        raise NotImplementedError()

    def __getitem__(self, name):
        if name not in self.data:
            self.data[name] = PlotData()
        return self.data[name]

    def iter_data(self):
        """Iterates through the plot data ordered by name"""
        for name in sorted(self.data):
            yield name, self[name]

    def _need_legend(self):
        return self.legend and len(self.data) > 1

    def __iter__(self):
        return self.data.itervalues()

    def _lim(self, default, index, opr):
        if default is None:
            return opr(opr(p[index] for p in pts) for pts in self)
        else:
            return int(default)

    @property
    def xmin(self):
        """Gets or computes the xmin"""
        return self._lim(self._xmin, 0, min)

    @xmin.setter
    def xmin(self, val):
        """Sets the default xmin"""
        self._xmin = val

    @property
    def xmax(self):
        """Gets or computes the xmax"""
        return self._lim(self._xmax, 0, max)

    @xmax.setter
    def xmax(self, val):
        """Sets the default xmax"""
        self._xmax = val

    @property
    def ymin(self):
        """Gets or computes the ymin"""
        return self._lim(self._ymin, 1, min)

    @ymin.setter
    def ymin(self, val):
        """Sets the default ymin"""
        self._ymin = val

    @property
    def ymax(self):
        """Gets or computes the ymax"""
        return self._lim(self._ymax, 1, max)

    @ymax.setter
    def ymax(self, val):
        """Sets the default ymax"""
        self._ymax = val
