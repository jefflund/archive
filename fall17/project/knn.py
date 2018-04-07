import pickle

import numpy as np
import sklearn.neighbors as skn
from tqdm import trange

train_data, test_data = pickle.load(open('ratings.pickle', 'rb'))
train_data = train_data.toarray()
num_users, num_movies = train_data.shape

knnr = skn.KNeighborsRegressor(algorithm='brute').fit(train_data, train_data)

step = 100
sse = 0
n = 0
for i in trange(0, num_users, step):
    j = i + step
    predictions = knnr.predict(train_data[i:j])
    actual = test_data[i:j]
    known = actual.nonzero()
    delta = predictions[known] - actual.toarray()[known]
    sse += np.sum(delta ** 2)
    n += delta.size
    print(sse/n)
