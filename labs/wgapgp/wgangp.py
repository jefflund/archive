import tensorflow as tf
import numpy as np

import itertools
from tqdm import trange
from scipy import misc
from glob import glob

D = 100 # z-space dimensions
W, H = 218, 178 # image dimensions
faces=glob('img_align_celeba/*.jpg')[:1]


def compute_stats():
    n, s = 0, 0
    for face in faces:
        x = misc.imread(face)
        n += x.size
        s += x.sum()
    mu = s / n

    s = 0
    for face in faces:
        x = misc.imread(face) - mu
        s += (x * x).sum()
    sig = np.sqrt(s / (n - 1))

    return mu, sig

# mu, sig = compute_stats()
mu, sig = 111.72252069779969, 76.832591022616995

def whiten(batch):
    return batch / 255
    # return (batch - mu) / sig

def unwhiten(batch):
    return (batch * 255).astype('uint8')
    # return (batch * sig + mu).astype('uint8')


faces_cycle = itertools.cycle(faces)

def get_batch(n):
    batch = itertools.islice(faces_cycle, n)
    batch = [misc.imread(fname) for fname in batch]
    batch  = np.asarray(batch)
    return whiten(batch)


def generator(z):
    with tf.variable_scope('Generator') as scope:
        if scope.trainable_variables():
            scope.reuse_variables()

        x = tf.contrib.layers.fully_connected(z, W*H*32, activation_fn=None)
        x = tf.reshape(x, [-1, W, H, 32])

        for _ in range(20):
            fx = tf.contrib.layers.conv2d(x, 32, kernel_size=3)
            fx = tf.contrib.layers.conv2d(fx, 32, kernel_size=3, activation_fn=None)
            x = tf.nn.relu(x + fx)

        x = tf.contrib.layers.conv2d(x, 3, kernel_size=3, activation_fn=tf.nn.sigmoid)

    return x


def discriminator(x):
    with tf.variable_scope('Discriminator') as scope:
        if scope.trainable_variables():
            scope.reuse_variables()

        y = tf.contrib.layers.conv2d(x, 32, kernel_size=3)

        for _ in range(20):
            fy = tf.contrib.layers.conv2d(y, 32, kernel_size=3)
            fy = tf.contrib.layers.conv2d(fy, 32, kernel_size=3, activation_fn=None)
            y = tf.nn.relu(y + fy)

        y = tf.contrib.layers.fully_connected(y, 1, activation_fn=tf.nn.tanh)

    return y


tf.reset_default_graph()

z = tf.placeholder(tf.float32, (None, D), name='z')
x = tf.placeholder(tf.float32, (None, W, H, 3), name='x')
e = tf.placeholder(tf.float32, (), name='e')

x_tilde = generator(z)
x_hat = e * x + (1 - e) * x_tilde
penalty = 10 * (tf.norm(tf.gradients(discriminator(x_hat), x_hat)) - 1) ** 2
dloss = tf.reduce_mean(discriminator(x_tilde) - discriminator(x) + penalty)

gloss = tf.reduce_mean(-discriminator(generator(z)))

summary = tf.summary.merge([
    tf.summary.scalar('Disc Loss', dloss),
    tf.summary.scalar('Gen Loss', gloss),
])

with tf.variable_scope('Discriminator') as scope:
    dvars = scope.trainable_variables()
    print(dvars)
with tf.variable_scope('Generator') as scope:
    gvars = scope.trainable_variables()
dtrain = tf.train.AdamOptimizer(.0001, 0, .9).minimize(dloss, var_list=dvars)
gtrain = tf.train.AdamOptimizer(.0001, 0, .9).minimize(gloss, var_list=gvars)

num_epochs = 25
batch_size = 5
data_size = 25 # len(faces)
epoch_interval = data_size // batch_size
num_steps = num_epochs * epoch_interval

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())
    writer = tf.summary.FileWriter('logs', sess.graph)

    def output(step):
        imgs = sess.run(generator(z), {
            z: np.random.random((batch_size, D)),
        })
        for i, img in enumerate(imgs):
            misc.imsave('output/{}-{}-rand.png'.format(step, i), unwhiten(img))

    for step in trange(num_steps):
        for t in range(5):
            sess.run(dtrain, {
                x: get_batch(batch_size),
                z: np.random.random((batch_size, D)),
                e: np.random.random(),
            })
        sess.run(gtrain, {
            z: np.random.random((batch_size, D)),
        })

        ss = sess.run(summary, {
            x: get_batch(batch_size),
            z: np.random.random((batch_size, D)),
            e: np.random.random(),
        })
        writer.add_summary(ss, step)
        writer.flush()

        if step % epoch_interval == 0:
            output(step)

    output('result')
    writer.close()
