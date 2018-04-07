import tensorflow as tf
import numpy as np

tf.reset_default_graph()

with tf.name_scope('generator'):
    x = tf.placeholder(tf.float32, shape=(), name='x')
    y = tf.placeholder(tf.float32, shape=(), name='y')

m = tf.get_variable('m', initializer=.1)
b = tf.get_variable('b', initializer=.1)
with tf.name_scope('estimator'):
    with tf.name_scope('delta'):
        delta = .005 * (y - (m * x + b))
    update_m = tf.assign(m, m + delta * x, name='assign_m')
    update_b = tf.assign(b, b + delta, name='assign_b')

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())

    for _ in range(1000):
        x_hat = np.random.uniform(-10, 10)
        eta_hat = np.random.uniform(-1, 1)
        y_hat = -6.7 * x_hat + 2 + eta_hat
        sess.run([update_m, update_b], {x: x_hat, y: y_hat})

    print('I guess the line is: y = {}*x + {}'.format(*sess.run([m, b])))

    writer = tf.summary.FileWriter('tf_logs', sess.graph)
    writer.close()
