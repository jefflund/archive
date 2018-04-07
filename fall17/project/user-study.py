import pickle

import tensorflow as tf
import numpy as np
import sklearn.neighbors as skn

print('loading data')
train_data, test_data = pickle.load(open('ratings.pickle', 'rb'))
train_data = train_data.toarray()
num_users, num_movies = test_data.shape
save_loc = 'model3/model.ckpt'
logs_loc = 'logs'

print('setup model')
x = tf.placeholder(tf.float32, (None, num_movies), name='x') # user profile
q = tf.placeholder(tf.int32, (None,), name='q') # movie query
y = tf.placeholder(tf.float32, (None,1), name='y') # movie rating

nn = tf.contrib.layers.fully_connected(x, 2048)
nn = tf.contrib.layers.fully_connected(nn, 1024)
nn = tf.contrib.layers.fully_connected(nn, 512)
nn = tf.contrib.layers.fully_connected(nn, 1024)
nn = tf.contrib.layers.fully_connected(nn, 2048)
nn = tf.contrib.layers.fully_connected(nn, num_movies, activation_fn=None)

mse = tf.losses.mean_squared_error(y, tf.gather(nn, q, axis=1))
train = tf.train.AdamOptimizer().minimize(mse)

print('setup knn')
knnr = skn.KNeighborsRegressor(algorithm='brute').fit(train_data, train_data)

with tf.Session() as sess:
    print('load params')
    saver = tf.train.Saver()
    saver.restore(sess, save_loc)

    print('sample users')
    users = np.random.choice(num_users, size=20, replace=False)
    profiles = train_data[users]

    print('predict model')
    model_pred = sess.run(nn, {x: profiles})
    print('predict knn')
    knn_pred = knnr.predict(profiles)

    print('dump data')
    pickle.dump((users, profiles, model_pred, knn_pred), open('asdf.pickle', 'wb'))
