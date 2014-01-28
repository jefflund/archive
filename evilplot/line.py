from __future__ import division

import pylab
import itertools


class AggLine(object):

    def __init__(self):
        self.lines = []

    def add_xys(self, xys):
        self.lines.append(xys)

    def add_from_column(self, columns, col_x, col_y):
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
        x, y = zip(*(l[index if len(l) > index else -1] for l in self.lines))
        return pylab.mean(x), pylab.mean(y)

    def __len__(self):
        return max(len(line) for line in self.lines)

    def __iter__(self):
        for i in range(len(self)):
            yield self[i]

    def plot(self, style='-', **kwargs):
        x, y = zip(*self)
        pylab.plot(x, y, style, **kwargs)

    def end_error(self, **kwargs):
        x, y = zip(*(l[-1] for l in self.lines))
        errx, erry = pylab.std(x), pylab.std(y)
        mux, muy = pylab.mean(x), pylab.mean(y)
        pylab.errorbar(mux, muy, erry, errx, **kwargs)

    def point_errors(self, **kwargs):
        xs, ys, errxs, errys = [], [], [], []
        for i in xrange(len(self)):
            x, y = zip(*(l[i if len(l) > i else -1] for l in self.lines))
            xs.append(pylab.mean(x))
            ys.append(pylab.mean(y))
            errxs.append(pylab.std(x))
            errys.append(pylab.std(y))
        pylab.errorbar(xs, ys, errys, errxs, **kwargs)


class LinePlot(object):

    def __init__(self, **kwargs):
        self.lines = {}

        self.xlabel = kwargs.get('xlabel', 'X')
        self.ylabel = kwargs.get('ylabel', 'Y')
        self.legend = kwargs.get('legend', 'best')
        self.title = kwargs.get('title')

        self.color_cycle = ['#00305E', '#007722', '#990011']
        self.style_cycle = ['-', '--', ':', '-.']

    def __getitem__(self, name):
        if name not in self.lines:
            self.lines[name] = AggLine()
        return self.lines[name]

    def add_xys(self, name, xys):
        self[name].add_xys(xys)

    def add_from_column(self, name, columns, col_x, col_y):
        self[name].add_from_column(columns, col_x, col_y)

    def _plot(self, end_error, point_errors):
        get_color = itertools.cycle(self.color_cycle).next
        get_style = itertools.cycle(self.style_cycle).next
        for name, line in self.lines.iteritems():
            color = get_color()
            style = get_style()
            line.plot(label=name, color=color, linestyle=style)
            if point_errors:
                line.point_errors(ecolor=color, label='_nolegend_')
            elif end_error:
                line.end_error(ecolor=color, label='_nolegend_')

        self._show_opt('xlabel')
        self._show_opt('ylabel')
        self._show_opt('title')
        if len(self.lines) > 1:
            self._show_opt('legend', 'loc')

    def show(self, end_error=False, point_errors=False):
        self._plot(end_error, point_errors)
        pylab.show()

    def save(self, filename, end_error=False, point_errors=False):
        self._plot(end_error, point_errors)
        pylab.savefig(filename)

    def _show_opt(self, opt_name, kwname=None):
        opt_val = getattr(self, opt_name)
        if opt_val:
            opt_call = getattr(pylab, opt_name)
            if kwname:
                opt_call(**{kwname: opt_val})
            else:
                opt_call(opt_val)
