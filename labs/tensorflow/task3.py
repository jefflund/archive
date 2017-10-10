import tensorflow as tf
import numpy as np

tf.reset_default_graph()

with tf.name_scope('generator'):
    x = tf.placeholder(tf.float32, shape=(3,), name='x')
    y = tf.placeholder(tf.float32, shape=(), name='y')

beta = tf.get_variable('beta', initializer=[0., 0., 0.])
with tf.name_scope('estimator'):
    with tf.name_scope('delta'):
        delta = .005 * (y - tf.reduce_sum(x * beta)) * x
    update_beta = tf.assign(beta, beta + delta, name='assign_beta')

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())

    for _ in range(1000):
        x_hat = np.append(np.random.uniform(-10, 10, size=(2,)), [1])
        eta_hat = np.random.uniform(-1, 1)
        y_hat = np.dot([-2.3, 4.5, 9.4], x_hat)
        sess.run(update_beta, {x: x_hat, y: y_hat})

    print('I guess beta is: {}'.format(sess.run(beta)))

    writer = tf.summary.FileWriter('tf_logs', sess.graph)
    writer.close()
