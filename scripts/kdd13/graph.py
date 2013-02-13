#!/usr/bin/python

from __future__ import division

import argparse
import os
import evilplot # not publicly released yet...this will soon change

def median(values):
    values = sorted(values)
    index = len(values) // 2
    return (values[index] + values[-index]) / 2


def mean(values):
    return sum(values) / len(values)


def parse_value(line):
    key, value = line.split()
    return key, float(value)


def sort_data(param, data, keys):
    return param, {key: [data[i][key] for i in sorted(data)] for key in keys}

def parse_file(filename):
    param = None
    data = {}
    keys = {'Time'}
    curr_iter = 0
    curr_time = 0

    with open(filename) as lines:
        for line in lines:
            if line.startswith('/usr/bin/xauth'):
                continue
            elif line.startswith('inference'):
                if param is not None:
                    yield sort_data(param, data, keys)
                    data = {}
                    keys = {'Time'}
                    curr_iter = 0
                    curr_time = 0
                param = line.split(None, 1)[1].strip()
            elif line[0].isdigit(): # read an iteration number
                curr_iter, time = parse_value(line)
                curr_iter = int(curr_iter)
                curr_time += time
            elif curr_iter > 0:
                if curr_iter not in data:
                    data[curr_iter] = {}
                    data[curr_iter]['Time'] = curr_time

                key, value = parse_value(line)
                data[curr_iter][key] = value
                keys.add(key)

    if param is not None:
        yield sort_data(param, data, keys)


def parse_files(opts):
    all_data = {}
    for dirpath in opts.data:
        for root, _, files in os.walk(dirpath):
            files = [os.path.join(root, filename) for filename in files]
            for filename in files:
                for param, data in parse_file(filename):
                    if param not in all_data:
                        all_data[param] = {}
                    for key, value in data.iteritems():
                        if key not in all_data[param]:
                            all_data[param][key] = []
                        all_data[param][key].append(value)

    for param in all_data:
        for key in all_data[param]:
            values = zip(*all_data[param][key])
            if opts.use_mean:
                values = [mean(value) for value in values]
            else:
                values = [median(value) for value in values]
            all_data[param][key] = values

    return all_data


def create_points(data, param, yname, xname='Time', style='lines'):
    data = list(zip(data[param][xname], data[param][yname]))
    return evilplot.Points(data, style=style, linewidth=1, title=param)


def create_plot(data, opts):
    plot = evilplot.Plot()
    yname = opts.yname
    xname = opts.xname
    style = opts.style
    no_anneal = opts.no_anneal
    for param in data:
        if no_anneal and param.startswith('anneal'):
            continue
        plot.append(create_points(data, param, yname, xname, style))
    return plot

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('data', nargs='+')
    parser.add_argument('--xname', default='Time')
    parser.add_argument('--yname', default='FM')
    parser.add_argument('--style', default='lines')
    parser.add_argument('--no-anneal', action='store_true', default=False)
    parser.add_argument('--use-mean', action='store_true', default=False)
    opts = parser.parse_args()

    data = parse_files(opts)
    create_plot(data, opts).show()

if __name__ == '__main__':
    main()
