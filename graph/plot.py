from __future__ import division

import pylab

def mean(values):
    return sum(values) / len(values)


def median(values):
    values = sorted(values)
    half_n = len(values) // 2
    return (values[half_n] + values[-half_n - 1]) / 2


class Plotter(object):

    def __init__(self):
        self.data = {}

    def _ensure_key(self, key):
        if key not in self.data:
            self.data[key] = []

    def append_data(self, key, value):
        self._ensure_key(key)
        self.data[key].append(value)

    def extend_data(self, key, values):
        self._ensure_key(key)
        self.data[key].extend(values)

    @classmethod
    def aggregate(cls, plotters, aggregate_func=median):
        data = {}
        for plotter in plotters:
            for key, values in plotter.data.iteritems():
                if key not in data:
                    data[key] = []
                data[key].append(values)

        aggregate_plotter = Plotter()
        for key, values in data.iteritems():
            values = [aggregate_func(xs) for xs in zip(*values)]
            aggregate_plotter.extend_data(key, values)
        return aggregate_plotter

    def _get_ykeys(self, xkey, opts):
        if 'ykey' in opts:
            return [opts['ykey']]
        elif 'ykeys' in opts:
            return opts['ykeys']
        else:
            return [key for key in self.data if key != xkey]

    def show(self, xkey, **opts):
        xdata = self.data[xkey]
        ykeys = self._get_ykeys(xkey, opts)

        for ykey in ykeys:
            ydata = self.data[ykey]
            pylab.plot(xdata, ydata, linewidth=1, label=ykey)

        pylab.show()

    def write_tikz(self, prefix, xkey, **opts):
        tex = open(prefix + '.tex', 'w')

        def write_opt(key, default=None):
            value = opts.get(key.replace(' ', '_'), default)
            if value is not None:
                print >> tex, '    {}={},'.format(key, value)

        def write_label(key):
            if key in opts:
                value = r'{{\small{{{}}}}}'.format(opts[key])
                print >> tex, '    {}={},'.format(key, value)

        def write_data(i, ykey):
            plot_cmd = r'\addplot +[mark=none] table[x index=0,y index=1]'
            print >> tex, r'{} {{{}-{}.dat}};'.format(plot_cmd, prefix, i)
            print >> tex, r'\addlegendentry{{\small {}}}'.format(ykey)
            dat = open('{}-{}.dat'.format(prefix, i), 'w')
            for x, y in zip(self.data[xkey], self.data[ykey]):
                print >> dat, x, y

        ykeys = self._get_ykeys(xkey, opts)

        print >> tex, r'\begin{tikzpicture}'
        print >> tex, r'\begin{axis}['
        print >> tex, r'    small,'
        write_opt('width', '3.4in')
        write_opt('height', '3.2in')
        write_opt('xmin', min(self.data[xkey]))
        write_opt('xmax', max(self.data[xkey]))
        write_opt('ymin', min(min(self.data[key]) for key in ykeys))
        write_opt('ymax', max(max(self.data[key]) for key in ykeys))
        write_opt('legend pos', 'south east')
        write_opt('legend cell align', 'left')
        write_opt('cycle list name')
        print >> tex, '    xlabel near ticks,'
        print >> tex, '    ylabel near ticks,'
        write_label('xlabel')
        write_label('ylabel')
        print >> tex, ']\n'

        for i, ykey in enumerate(ykeys):
            write_data(i, ykey)

        print >> tex, r'\end{axis}'
        print >> tex, r'\end{tikzpicture}'
