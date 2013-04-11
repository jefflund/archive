import pylab

class RunData(object):

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
