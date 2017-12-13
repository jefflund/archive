import os
import pickle

from scipy import sparse
import numpy as np
import tensorflow as tf

from tqdm import trange, tqdm

ratings = pickle.load(open('ratings.pickle', 'rb'))
num_users, num_movies = ratings.shape

def get_batch(n=50):
    x = ratings[np.random.choice(num_users, size=n, replace=True)]
    q = np.zeros(n, dtype=np.int32)
    y = np.zeros((n, 1), dtype=np.float32)
    for i in range(n):
        q[i] = np.random.choice(x[i].nonzero()[1])
        x[i, q[i]], y[i, 0] = 0, x[i, q[i]]
    return x.todense(), q, y


tf.reset_default_graph()

x = tf.placeholder(tf.float32, (None, num_movies), name='x') # user profile
q = tf.placeholder(tf.int32, (None,), name='q') # movie query
y = tf.placeholder(tf.float32, (None,1), name='y') # movie rating

n = tf.concat([x, tf.one_hot(q, num_movies)], axis=1)
n = tf.contrib.layers.fully_connected(n, 1028)
n = tf.contrib.layers.fully_connected(n, 512)
n = tf.contrib.layers.fully_connected(n, 256)
n = tf.contrib.layers.fully_connected(n, 128)
n = tf.contrib.layers.fully_connected(n, 64)
n = tf.contrib.layers.fully_connected(n, 32)
n = tf.contrib.layers.fully_connected(n, 1, activation_fn=None)

mse = tf.losses.mean_squared_error(y, n)
train = tf.train.AdamOptimizer().minimize(mse)

summary = tf.summary.scalar('mse', mse)

num_examples = ratings.nnz
num_epochs = 10
batch_size = 1000
num_steps = num_examples * num_epochs // batch_size
save_interval = num_examples // batch_size

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())
    writer = tf.summary.FileWriter('logs', sess.graph)
    saver = tf.train.Saver()

    for step in trange(num_steps):
        feed_x, feed_q, feed_y = get_batch()
        _, ss = sess.run([train, summary], {x: feed_x, q: feed_q, y: feed_y})
        writer.add_summary(ss, step)
        if step % save_interval == 0:
            saver.save(sess, 'model/model.ckpt')

    saver.save(sess, 'model/model.ckpt')
    writer.close()
