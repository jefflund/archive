import numpy as np
from scipy import sparse
import tensorflow as tf

movielens = '/users/data/ml-latest/ratings.csv'
raw = np.genfromtxt(movielens, delimiter=',', skip_header=1)
# The raw data is 1 index, so offset by 1 to zero index
num_users = raw[:, 0].max() - 1
num_movies = raw[:, 0].max() - 1

ratings = sparse.lil_matrix((num_users, num_movies))
for user, movie, rating, _ in raw:
    ratings[user, movie] = rating

tf.reset_default_graph()

x = tf.placeholder(tf.float32, (None, num_movies), name='x')
q = tf.placeholder(tf.float32, (), name = 'q')
y = tf.placeholder(tf.float32, (), name='y')

s = tf.concat((x, y), -1)
s = tf.contrib.layers.fully_connected(s, num_movies * 5)
s = tf.contrib.layers.fully_connected(s, num_movies * 5, activation_fn=None)

with tf.Session()
