#!/usr/bin/python3

from scipy.misc import imread, imresize, imsave
import numpy as np
import os
from glob import glob

D = 224
styles = glob('style/*.png')
contents = glob('content/*.png')
out_name = lambda s, c: 'output/{}-{}.png'.format(s[6:-4], c[8:-4])

for style in styles:
    for content in contents:
        os.system('./transfer.py {} {} {}'.format(style, content, out_name(style, content)))

grid = np.zeros((D * (len(styles)+1), D * (len(contents)+1), 3), dtype='uint8')

for i, style in enumerate(styles, 1):
    grid[i*D: (i+1)*D, 0:D] = imresize(imread(style, mode='RGB'), (D, D))
for j, content in enumerate(contents, 1):
    grid[0:D, j*D:(j+1)*D] = imresize(imread(content, mode='RGB'), (D, D))

for i, style in enumerate(styles, 1):
    for j, content in enumerate(contents, 1):
        grid[i*D: (i+1)*D, j*D:(j+1)*D] = imread(out_name(style, content), mode='RGB')

imsave('grid.png', grid)
