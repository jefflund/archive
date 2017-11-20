#!/usr/bin/python3

import numpy as np
import tensorflow as tf
import vgg16
from scipy.misc import imread, imresize, imsave
from tqdm import tqdm, trange
import sys

style_img_name = sys.argv[1] if len(sys.argv) > 1 else 'style.png'
content_img_name = sys.argv[2] if len(sys.argv) > 1 else 'content.png'
output_img_name = sys.argv[3] if len(sys.argv) > 1 else 'output.png'

sess = tf.Session()

opt_img = tf.Variable(tf.truncated_normal([1,224,224,3],
                                          dtype=tf.float32,
                                          stddev=1e-1), name='opt_img')
tmp_img = tf.clip_by_value(opt_img, 0.0, 255.0)

vgg = vgg16.vgg16(tmp_img, 'vgg16_weights.npz', sess)

style_img = imread(style_img_name, mode='RGB')
style_img = imresize(style_img, (224, 224))
style_img = np.reshape(style_img, [1,224,224,3])

content_img = imread(content_img_name, mode='RGB')
content_img = imresize(content_img, (224, 224))
content_img = np.reshape(content_img, [1,224,224,3])

layers = ['conv1_1', 'conv1_2',
          'conv2_1', 'conv2_2',
          'conv3_1', 'conv3_2', 'conv3_3',
          'conv4_1', 'conv4_2', 'conv4_3',
          'conv5_1', 'conv5_2', 'conv5_3']
ops = [getattr(vgg, x) for x in layers]

content_acts = sess.run(ops, feed_dict={vgg.imgs: content_img})
style_acts = sess.run(ops, feed_dict={vgg.imgs: style_img})

def gram(x, M, N):
    x = tf.reshape(x, (M, N))
    return tf.matmul(tf.transpose(x), x)

def content_layer_loss(layer):
    F, P = ops[layer], content_acts[layer]
    return tf.reduce_sum(tf.square(F - P)) / 2

def style_layer_loss(layer):
    F, P = style_acts[layer], ops[layer]
    _, h, w, N = F.shape
    M = w * h
    G, A = gram(F, M, N), gram(P, M, N)
    return tf.reduce_sum(tf.square(G - A)) / (4 * M**2 * N**2)


content_layer = 8
style_layers = [0, 2, 4, 7, 10]
style_weights = np.ones(len(style_layers)) / len(style_layers)
alpha, beta = 1, 1e3

content_loss = content_layer_loss(content_layer)
style_loss = sum(w * style_layer_loss(l) for l , w in zip(style_layers, style_weights))
total_loss = alpha * content_loss + beta * style_loss

train = tf.train.AdamOptimizer(.1).minimize(total_loss, var_list=[opt_img])

sess.run(tf.global_variables_initializer())
vgg.load_weights('vgg16_weights.npz', sess)
sess.run(opt_img.assign(content_img))

for i in trange(6000):
    if i % 100 == 0:
        img = sess.run(opt_img)
        img = tf.clip_by_value(img, 0.0, 255.0)
        sess.run(opt_img.assign(img))

    sess.run(train)

imsave(output_img_name, sess.run(opt_img)[0])
