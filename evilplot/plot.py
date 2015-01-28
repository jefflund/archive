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
        x, y = zip(*(l[index if len(l) > index else -1] for l in self.data if l))
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

        self.xmin = kwargs.get('xmin')
        self.xmax = kwargs.get('xmax')
        self.ymin = kwargs.get('ymin')
        self.ymax = kwargs.get('ymax')

        self.xlabel = kwargs.get('xlabel')
        self.ylabel = kwargs.get('ylabel')

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
        self.pylab_plot()
        self._pylab_opts()
        pylab.show()

    def save(self, filename):
        """Uses pylab to save the plot"""
        self.pylab_plot()
        self._pylab_opts()
        pylab.savefig(filename)

    def _pylab_opts(self):
        """Sets the plot options for pylab"""
        if self.xlabel:
            pylab.xlabel(self.xlabel)
        if self.ylabel:
            pylab.ylabel(self.ylabel)

        if self.xmax:
            pylab.xlim(xmax=int(self.xmax))
        if self.xmin:
            pylab.xlim(xmin=int(self.xmin))
        if self.ymax:
            pylab.ylim(ymax=int(self.ymax))
        if self.ymin:
            pylab.ylim(ymin=int(self.ymin))

        if self._need_legend():
            pylab.legend(loc=_LEGEND_PYPLOT.get(self.legend, 'best'))

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
        print >> texfile, r'\begin{axis}['

        if self.xlabel:
            print >> texfile, r'    xlabel={\small %s},' % self.xlabel
        if self.ylabel:
            print >> texfile, r'    ylabel={\small %s},' % self.ylabel

        if self.xmin:
            print >> texfile, r'    xmin=%d,' % int(self.xmin)
        if self.xmax:
            print >> texfile, r'    xmax=%d,' % int(self.xmax)
        if self.ymin:
            print >> texfile, r'    ymin=%d,' % int(self.ymin)
        if self.ymax:
            print >> texfile, r'    ymax=%d,' % int(self.ymax)

        if self._need_legend():
            legend_pos = _LEGEND_PGF.get(self.legend)
            if legend_pos:
                print >> texfile, r'    legend pos=%s,' % legend_pos
            print >> texfile, r'    legend cell align=left,'

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

_LEGEND_PGF = {
        'ur': 'north east',
        'ul': 'north west',
        'll': 'south west',
        'lr': 'south east',
        }
_LEGEND_PYPLOT = {
        'ur': 1,
        'ul': 2,
        'll': 3,
        'lr': 4,
        }
