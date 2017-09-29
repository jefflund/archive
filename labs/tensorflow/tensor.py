import tensorflow as tf
import numpy as np

tf.reset_default_graph()

with tf.name_scope('generator'):
    x = tf.random_uniform((), -10, 10, name='x')
    eta = tf.random_uniform((), -1, 1, name='eta')
    y = -6.7 * x + 2 + eta

sess = tf.Session()
sess.run(tf.global_variables_initializer())

for _ in range(100):
    sess.run(y)

writer = tf.summary.FileWriter('tf_logs', sess.graph)
writer.close()
