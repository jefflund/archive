from __future__ import division

import pylab


def mean(xs):
    return sum(xs) / len(xs)


class AggLine(object):

    def __init__(self, agg_x=mean, agg_y=mean):
        self.lines = []
        self.agg_x = agg_x
        self.agg_y = agg_y

    def add_xys(self, xys):
        self.lines.append(xys)

    def add_from_column(self, columns, col_x, col_y):
        xys = []
        for line in columns:
            line = line.split()
            xy = float(line[col_x]), float(line[col_y])
            xys.append(xy)
        self.add_xys(xys)

    def __getitem__(self, index):
        x, y = zip(*(line[index] for line in self.lines if len(line) > index))
        return self.agg_x(x), self.agg_y(y)

    def __len__(self):
        return max(len(line) for line in self.lines)

    def __iter__(self):
        for i in range(len(self)):
            yield self[i]


class Plot(object):

    def __init__(self, agg_x=mean, agg_y=mean, **kwargs):
        self.lines = {}
        self.agg_x = agg_x
        self.agg_y = agg_y

        self.xlabel = kwargs.get('xlabel', 'X')
        self.ylabel = kwargs.get('ylabel', 'Y')
        self.legend = kwargs.get('legend', 'best')
        self.title = kwargs.get('title')

    def __getitem__(self, name):
        if name not in self.lines:
            self.lines[name] = AggLine(self.agg_x, self.agg_y)
        return self.lines[name]

    def add_xys(self, name, xys):
        self[name].add_xys(xys)

    def add_from_column(self, name, columns, col_x, col_y):
        self[name].add_from_column(columns, col_x, col_y)

    def show(self):
        for name, line in self.lines.iteritems():
            x, y = zip(*line)
            pylab.plot(x, y, label=name)

        self._show_opt('xlabel')
        self._show_opt('ylabel')
        self._show_opt('legend', 'loc')
        self._show_opt('title')

        pylab.show()

    def _show_opt(self, opt_name, kwname=None):
        opt_val = getattr(self, opt_name)
        if opt_val:
            opt_call = getattr(pylab, opt_name)
            if kwname:
                opt_call(**{kwname: opt_val})
            else:
                opt_call(opt_val)
