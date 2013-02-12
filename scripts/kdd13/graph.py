from __future__ import division

import os

def median(values):
    values = sorted(values)
    index = len(values) // 2
    return (values[index] + values[-index]) / 2


def parse_value(line):
    key, value = line.split()
    return key, float(value)


def parse_file(filename):
    params = None
    times = []
    data = {}
    curr_iter = 0

    with open(filename) as lines:
        for line in lines:
            if line.startswith('/usr/bin/xauth'):
                continue
            elif line.startswith('inference'):
                params = ' '.join(eval(line.split()[1]))
            elif line[0].isdigit(): # read an iteration number
                curr_iter, time = parse_value(line)
                curr_iter = int(curr_iter)
                times.append(time)
            elif 0 < curr_iter <= 1000:# if we're past the header
                key, value = parse_value(line)
                if key not in data:
                    data[key] = {}
                data[key][curr_iter] = value

    return params, times, data


def parse_files(dirpath):
    all_times = {}
    all_data = {}
    for root, _, files in os.walk(dirpath):
        files = [os.path.join(root, filename) for filename in files]
        for filename in files:
            params, times, data = parse_file(filename)
            if params is None:
                continue

            if params not in all_data:
                all_data[params] = {}
                all_times[params] = []

            all_times[params].extend(times)
            for key, values in data.iteritems():
                if key not in all_data[params]:
                    all_data[params][key] = []
                all_data[params][key].append(values)

    return all_times, all_data


def combine_data(times, data):
    for param, values in times.iteritems():
        times[param] = median(values)

    # start: data[inference][metric][run_num][iter_num] = value
    # goal:  data[inference][metric][iter_index] = median value
    for param in data:
        for metric in data[param]:
            values = []
            for iter_num in data[param][metric][0]: # iter_num from first run
                value = median(run[iter_num] for run in data[param][metric])
                values.append(value)
            data[param][metric] = values

    return times, data
