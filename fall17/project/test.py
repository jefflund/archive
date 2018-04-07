import pickle

import tensorflow as tf
import numpy as np

from tqdm import trange

train_data, test_data = pickle.load(open('ratings.pickle', 'rb'))
train_data = train_data.toarray()
num_users, num_movies = test_data.shape
save_loc = 'model3/model.ckpt'
logs_loc = 'logs'

x = tf.placeholder(tf.float32, (None, num_movies), name='x') # user profile
q = tf.placeholder(tf.int32, (None,), name='q') # movie query
y = tf.placeholder(tf.float32, (None,1), name='y') # movie rating

nn = tf.contrib.layers.fully_connected(x, 2048)
nn = tf.contrib.layers.fully_connected(nn, 1024)
nn = tf.contrib.layers.fully_connected(nn, 512)
nn = tf.contrib.layers.fully_connected(nn, 1024)
nn = tf.contrib.layers.fully_connected(nn, 2048)
nn = tf.contrib.layers.fully_connected(nn, num_movies, activation_fn=None)

mse = tf.losses.mean_squared_error(y, tf.gather(nn, q, axis=1))
train = tf.train.AdamOptimizer().minimize(mse)

step = 100
sse = 0
n = 0

with tf.Session() as sess:
    saver = tf.train.Saver()
    saver.restore(sess, save_loc)

    for i in trange(0, num_users, step):
        j = i + step
        predictions = sess.run(nn, {x: train_data[i:j]})
        actual = test_data[i:j]
        known = actual.nonzero()
        delta = predictions[known] - actual.toarray()[known]
        sse += np.sum(delta ** 2)
        n += delta.size
        print(sse/n)
