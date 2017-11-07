import numpy as np
import tensorflow as tf

import collections
from tqdm import trange

num_epochs = 1000
batch_size = 50
sequence_length = 50
state_dim = 128
num_layers = 2

def load_data(input_file):
    with open(input_file) as f:
        data = f.read()

    vocab = collections.Counter(data)
    vocab = sorted(vocab, key=vocab.get, reverse=True)
    vocab = {v: i for i, v in enumerate(vocab)}

    num_batches = len(data) // (batch_size * sequence_length)
    xs = list(map(vocab.get, data))[:num_batches * batch_size * sequence_length]
    ys = xs[1:] + xs[:1]
    xs = np.split(np.reshape(xs, (batch_size, -1)), num_batches, 1)
    ys = np.split(np.reshape(ys, (batch_size, -1)), num_batches, 1)
    batches = list(zip(xs, ys))

    return vocab, batches


vocab, batches = load_data('alma.txt')
vocab_size = len(vocab)

tf.reset_default_graph()

in_ph = tf.placeholder(tf.int32, [None, sequence_length], name='inputs')
targ_ph = tf.placeholder(tf.int32, [None, sequence_length], name='targets')
in_onehot = tf.one_hot(in_ph, vocab_size, name='input_onehot')

inputs = tf.split(in_onehot, sequence_length, axis=1)
inputs = [tf.squeeze(input_, [1]) for input_ in inputs]
targets = tf.split(targ_ph, sequence_length, axis=1)

cells = [tf.contrib.rnn.BasicRNNCell(state_dim) for _ in range(num_layers)]
cells = tf.contrib.rnn.MultiRNNCell(cells)
initial_state = cells.zero_state(batch_size, tf.float32)

outputs, final_state = tf.contrib.legacy_seq2seq.rnn_decoder(inputs, initial_state, cells)
outputs = tf.reshape(tf.concat(outputs, 1), [-1, state_dim])

logits = tf.contrib.layers.fully_connected(outputs, vocab_size, activation_fn=None)
loss = tf.contrib.legacy_seq2seq.sequence_loss([logits], [targets], [tf.ones([batch_size * sequence_length])])
train = tf.train.AdamOptimizer().minimize(tf.reduce_mean(loss))

summary = tf.summary.scalar('loss', loss)

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())
    writer = tf.summary.FileWriter('logs', sess.graph)

    for epoch in trange(num_epochs):
        state = sess.run(initial_state)
        for x, y in batches:
            feed = {in_ph: x, targ_ph: y}
            for k, s in enumerate(initial_state):
                feed[s] = state[k]
            ops = [train, summary, list(final_state)]
            _, ss, state = sess.run(ops, feed)
            writer.add_summary(ss)
            writer.flush()

    writer.close()
