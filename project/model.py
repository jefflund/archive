import pickle

import tensorflow as tf
import numpy as np

from tqdm import trange

train_data, test_data = pickle.load(open('ratings.pickle', 'rb'))
num_users, num_movies = train_data.shape
save_loc = 'model3/model.ckpt'
logs_loc = 'logs'

def get_batch(data, n):
    x = data[np.random.choice(num_users, size=n, replace=True)]
    x = x[list(set(x.nonzero()[0]))]
    n = x.shape[0]

    q = np.zeros(n, dtype=np.int32)
    y = np.zeros((n, 1), dtype=np.float32)

    for i in range(n):
        choices = x[i].nonzero()[1]
        q[i] = np.random.choice(x[i].nonzero()[1])
        x[i, q[i]], y[i, 0] = 0, x[i, q[i]]

    return x.todense(), q, y

x = tf.placeholder(tf.float32, (None, num_movies), name='x') # user profile
q = tf.placeholder(tf.int32, (None,), name='q') # movie query
y = tf.placeholder(tf.float32, (None,1), name='y') # movie rating

n = tf.contrib.layers.fully_connected(x, 2048)
n = tf.contrib.layers.fully_connected(n, 1024)
n = tf.contrib.layers.fully_connected(n, 512)
n = tf.contrib.layers.fully_connected(n, 1024)
n = tf.contrib.layers.fully_connected(n, 2048)
n = tf.contrib.layers.fully_connected(n, num_movies, activation_fn=None)

mse = tf.losses.mean_squared_error(y, tf.gather(n, q, axis=1))
train = tf.train.AdamOptimizer().minimize(mse)

summary = tf.summary.scalar('mse', mse)

num_examples = train_data.nnz
num_epochs = 10
batch_size = 1000
num_steps = num_examples * num_epochs // batch_size
save_interval = num_examples // batch_size

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())
    writer = tf.summary.FileWriter(logs_loc, sess.graph)
    saver = tf.train.Saver()

    saver.restore(sess, save_loc) # continue from previous 20 epochs

    for step in trange(num_steps):
        feed_x, feed_q, feed_y = get_batch(train_data, batch_size)
        _, ss = sess.run([train, summary], {x: feed_x, q: feed_q, y: feed_y})
        writer.add_summary(ss, step+2*num_steps) # add in prev 20 epochs
        if step % save_interval == 0:
            saver.save(sess, save_loc)

    saver.save(sess, save_loc)
    writer.close()
