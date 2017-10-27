
# coding: utf-8

# In[9]:


import tensorflow as tf
import numpy as np

import itertools
from tqdm import trange
from scipy import misc
from glob import glob


# In[13]:


D = 100
W, H = 218, 178


# In[21]:


faces = glob('img_align_celeba/*.jpg')[:16]
faces = [misc.imread(fname) / 255 for fname in faces]
faces = np.array(faces)


# In[22]:


def generator(z):
    with tf.variable_scope('Generator') as scope:
        if scope.trainable_variables():
            scope.reuse_variables()

        x = tf.contrib.layers.fully_connected(z, W*H*32, activation_fn=None)
        x = tf.reshape(x, [-1, W, H, 32])

        for _ in range(10):
            fx = tf.contrib.layers.conv2d(x, 32, kernel_size=3)
            fx = tf.contrib.layers.conv2d(fx, 32, kernel_size=3, activation_fn=None)
            x = tf.nn.relu(x + fx)

        x = tf.contrib.layers.conv2d(x, 3, kernel_size=3, activation_fn=tf.nn.sigmoid)

    return x

def generator_vars():
    with tf.variable_scope('Generator') as scope:
        return scope.trainable_variables()


# In[23]:


def discriminator(x):
    with tf.variable_scope('Discriminator') as scope:
        if scope.trainable_variables():
            scope.reuse_variables()

        y = tf.contrib.layers.conv2d(x, 32, kernel_size=3)

        for _ in range(10):
            fy = tf.contrib.layers.conv2d(y, 32, kernel_size=3)
            fy = tf.contrib.layers.conv2d(fy, 32, kernel_size=3, activation_fn=None)
            y = tf.nn.relu(y + fy)

        y = tf.contrib.layers.fully_connected(y, 1, activation_fn=tf.nn.tanh)

    return y

def discriminator_vars():
    with tf.variable_scope('Discriminator') as scope:
        return scope.trainable_variables()


# In[24]:


tf.reset_default_graph()

z = tf.placeholder(tf.float32, (None, D), name='z')
x = tf.placeholder(tf.float32, (None, W, H, 3), name='x')
e = tf.placeholder(tf.float32, (None, 1, 1, 1), name='e')

x_tilde = generator(z)
x_hat = e * x + (1 - e) * x_tilde
penalty = 10 * (tf.norm(tf.gradients(discriminator(x_hat), x_hat)) - 1) ** 2
dloss = tf.reduce_mean(discriminator(x_tilde) - discriminator(x) + penalty)
gloss = tf.reduce_mean(-discriminator(generator(z)))

summary = tf.summary.merge([
    tf.summary.scalar('Disc_Loss', dloss),
    tf.summary.scalar('Gen_Loss', gloss),
])

dtrain = tf.train.AdamOptimizer(.0001, 0, .9).minimize(dloss, var_list=discriminator_vars())
gtrain = tf.train.AdamOptimizer(.0001, 0, .9).minimize(gloss, var_list=generator_vars())


# In[25]:


num_epochs = 300
batch_size = 16
data_size = len(faces)
num_steps = num_epochs * data_size // batch_size
output_interval = 10

def gen_input():
    return {
        x: faces[np.random.choice(data_size, batch_size)],
        z: np.random.random((batch_size, D)),
        e: np.random.random((batch_size, 1, 1, 1))
    }


# In[ ]:


with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())
    writer = tf.summary.FileWriter('logs', sess.graph)
    saver = tf.train.Saver()

    def output(step):
        imgs = sess.run(generator(z), {
            z: np.random.random((1, D)),
        })
        for i, img in enumerate(imgs):
            misc.imsave('output/{}-{}-rand.png'.format(step, i), (img * 255).astype('uint8'))

    for step in trange(num_steps):
        for t in range(5):
            sess.run(dtrain, gen_input())
        sess.run(gtrain, gen_input())

        ss = sess.run(summary, gen_input())
        writer.add_summary(ss, step)
        writer.flush()

        if step % output_interval == 0:
            output(step)
            saver.save(sess, 'model/model.ckpt')

    output('result')
    writer.close()

