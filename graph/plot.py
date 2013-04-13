import pylab

def mean(values):
    return sum(values) / len(values)

def median(values):
    values = sorted(values)
    half_n = len(values) // 2
    return (values[half_n] + values[-half_n]) / 2

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

    def show(self, xkey, *ykeys):
        xdata = self.data[xkey]

        for ykey in ykeys:
            ydata = self.data[ykey]
            pylab.plot(xdata, ydata, linewidth=1, label=ykey)

        pylab.show()

    @classmethod
    def aggregate(cls, plotters, aggregate_func=median, **overrides):
        data = {}
        for plotter in plotters:
            for key, values in plotter.data.iteritems():
                if key not in data:
                    data[key] = []
                data[key].append(values)
