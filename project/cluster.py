import pickle

import tensorflow as tf
import numpy as np

from tqdm import trange

train_data, test_data = pickle.load(open('ratings.pickle', 'rb'))
num_users, num_movies = test_data.shape
save_loc = 'model3/model.ckpt'
logs_loc = 'logs'

x = tf.placeholder(tf.float32, (None, num_movies), name='x') # user profile
q = tf.placeholder(tf.int32, (None,), name='q') # movie query
y = tf.placeholder(tf.float32, (None,1), name='y') # movie rating

nn = tf.contrib.layers.fully_connected(x, 2048)
nn = tf.contrib.layers.fully_connected(nn, 1024)
nn = tf.contrib.layers.fully_connected(nn, 512)
bottleneck = nn
nn = tf.contrib.layers.fully_connected(nn, 1024)
nn = tf.contrib.layers.fully_connected(nn, 2048)
nn = tf.contrib.layers.fully_connected(nn, num_movies, activation_fn=None)

mse = tf.losses.mean_squared_error(y, tf.gather(nn, q, axis=1))
train = tf.train.AdamOptimizer().minimize(mse)

sess = tf.Session()
saver = tf.train.Saver()
saver.restore(sess, save_loc)

def learn_cluster(n):
    cluster_hot = np.zeros((1, 512))
    cluster_hot[0, n] = 100
    cluster_loss = tf.nn.softmax_cross_entropy_with_logits(labels=cluster_hot, logits=bottleneck)
    cluster_grad = tf.gradients(cluster_loss, x)
    learn_cluster = x - tf.multiply(cluster_grad[0], 1)

    x0 = np.random.normal(scale=1e-1, size=[1, num_movies])
    for _ in trange(10):
        x0 = sess.run(learn_cluster, {x: x0})
    return x0
