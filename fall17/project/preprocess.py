import pickle

import pandas as pd
import scipy.sparse as sp

data_path = '/users/data/ml-latest/ratings.csv'
pickle_path = 'ratings.pickle'

df = pd.read_csv(data_path)
num_users = df.userId.unique().shape[0]
num_movies = df.movieId.unique().shape[0]
split = df['timestamp'] < df.quantile(.9).timestamp
user_index, movie_index = dict(), dict()

train_data = sp.lil_matrix((num_users, num_movies))
for _, userId, movieId, rating, _ in df.loc[split].itertuples():
    userId = user_index.setdefault(userId, len(user_index))
    movieId = movie_index.setdefault(movieId, len(movie_index))
    train_data[userId, movieId] = rating
train_data = train_data.tocsr()

test_data = sp.lil_matrix((num_users, num_movies))
for _, userId, movieId, rating, _ in df.loc[-split].itertuples():
    userId = user_index.setdefault(userId, len(user_index))
    movieId = movie_index.setdefault(movieId, len(movie_index))
    test_data[userId, movieId] = rating
test_data = test_data.tocsr()

pickle.dump((train_data, test_data), open(pickle_path, 'wb'))
