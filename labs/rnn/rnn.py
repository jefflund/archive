# Imports and stuff
import collections

import numpy as np
import tensorflow as tf
from tensorflow.python.layers import core as layers_core
from tensorflow.python.ops.rnn_cell_impl import _linear, RNNCell

from tqdm import trange

# Global variables are evil...
num_epochs = 1000
print_interval = 100
batch_size = 50
sequence_length = 50
state_dim = 128
num_layers = 2

class GRUCell(RNNCell):

    def __init__(self, num_units, reuse=None):
        super(GRUCell, self).__init__(_reuse=reuse)
        self._num_units = num_units
        self._z = None
        self._r = None
        self._h = None

    @property
    def state_size(self):
        return self._num_units

    @property
    def output_size(self):
        return self._num_units

    def __call__(self, inputs, state):
        with tf.variable_scope('z'):
            z_t = tf.nn.sigmoid(_linear([inputs, state], self._num_units, True))
        with tf.variable_scope('r'):
            r_t = tf.nn.sigmoid(_linear([inputs, state], self._num_units, True))
        with tf.variable_scope('h'):
            reset = r_t * state
            h_t = tf.nn.tanh(z_t * state + (1 - z_t) * _linear([inputs, reset], self._num_units, True))

        return h_t, h_t

# Yup...I just put my whole stinkin lab in a single function so I could run it
# on multiple input files...
def do_lab8(input_file, prime):

    def load_data(input_file):
        with open(input_file) as f:
            data = f.read()

        chars = collections.Counter(data)
        chars = sorted(chars, key=chars.get, reverse=True)
        vocab = {v: i for i, v in enumerate(chars)}

        num_batches = len(data) // (batch_size * sequence_length)
        xs = list(map(vocab.get, data))[:num_batches * batch_size * sequence_length]
        ys = xs[1:] + xs[:1]
        xs = np.split(np.reshape(xs, (batch_size, -1)), num_batches, 1)
        ys = np.split(np.reshape(ys, (batch_size, -1)), num_batches, 1)
        batches = list(zip(xs, ys))

        return vocab, chars, batches

    vocab, chars, batches = load_data(input_file)
    vocab_size = len(vocab)

    tf.reset_default_graph()

    # Training inputs/targets
    in_ph = tf.placeholder(tf.int32, [batch_size, sequence_length], name='inputs')
    targ_ph = tf.placeholder(tf.int32, [batch_size, sequence_length], name='targets')
    in_onehot = tf.one_hot(in_ph, vocab_size, name='intput_onehot')
    inputs = tf.split(in_onehot, sequence_length, axis=1)
    inputs = [tf.squeeze(input_, [1]) for input_ in inputs]
    targets = tf.split(targ_ph, sequence_length, axis=1)
    targets = tf.reshape(tf.stack(targets), [sequence_length, batch_size])

    # LSTM network
    # cells = [tf.contrib.rnn.BasicRNNCell(state_dim) for _ in range(num_layers)]
    cells = [GRUCell(state_dim) for _ in range(num_layers)]
    cells =  tf.contrib.rnn.MultiRNNCell(cells)
    linear = layers_core.Dense(vocab_size, use_bias=True)

    # Decoder and output for training
    initial_state = cells.zero_state(batch_size, tf.float32)
    helper = tf.contrib.seq2seq.TrainingHelper(inputs, [sequence_length] * batch_size, time_major=True)
    decoder = tf.contrib.seq2seq.BasicDecoder(cells, helper, initial_state, output_layer=linear)
    outputs, final_state, _ = tf.contrib.seq2seq.dynamic_decode(decoder, output_time_major=True)
    logits = outputs.rnn_output

    # Optimize training loss
    loss = tf.contrib.seq2seq.sequence_loss(logits, targets, tf.ones([batch_size, sequence_length]))
    loss = tf.reduce_mean(loss)
    train = tf.train.AdamOptimizer().minimize(loss)
    summary = tf.summary.scalar('loss', loss)

    # Sampling inputs
    s_in_ph = tf.placeholder(tf.int32, [1], name='s_inputs')
    s_in_onehot = tf.one_hot(s_in_ph, vocab_size, name='s_intput_onehot')
    s_inputs = tf.split(s_in_onehot, 1, axis=1)

    # Sampling decoder and output
    s_initial_state = cells.zero_state(1, tf.float32)
    s_helper = tf.contrib.seq2seq.TrainingHelper(s_inputs, [1], time_major=True)
    s_decoder = tf.contrib.seq2seq.BasicDecoder(cells, s_helper, s_initial_state, output_layer=linear)
    s_outputs, s_final_state, _ = tf.contrib.seq2seq.dynamic_decode(s_decoder, output_time_major=True)
    s_probs = tf.nn.softmax(s_outputs.rnn_output)

    sess = tf.Session()
    sess.run(tf.global_variables_initializer())

    def train_epoch():
        state = sess.run(initial_state)
        for x, y in batches:
            feed = {in_ph: x, targ_ph: y}
            for k, s in enumerate(initial_state):
                feed[s] = state[k]
            _, state = sess.run([train, final_state], feed)

    def sample(sample_size, prime):
        state = sess.run(s_initial_state)

        for char in prime[:-1]:
            feed = {s_in_ph: np.ravel(vocab[char]).astype(np.int32)}
            for i, s in enumerate(s_initial_state):
                feed[s] = state[i]
            state = sess.run(s_final_state, feed)

        result = prime
        char = prime[-1]
        for _ in range(sample_size):
            feed = {s_in_ph: np.ravel(vocab[char]).astype(np.int32)}
            for i, s in enumerate(s_initial_state):
                feed[s] = state[i]
            probs, state = sess.run([s_probs, s_final_state], feed)

            sample = np.random.choice(vocab_size, p=probs[0][0])
            pred = chars[sample]
            result += pred
            char = pred

        return result

    print('Training on {}'.format(input_file))
    epochs = trange(num_epochs)
    for epoch in epochs:
        train_epoch()
        if epoch % print_interval == 0:
            epochs.write('{} {}\n'.format(epoch, sample(150, prime)))

    print('Samples for {}'.format(input_file))
    for i in range(15):
        print(i, sample(150, prime))

do_lab8('alma.txt', 'And ')
do_lab8('beowulf.txt', '\n')
