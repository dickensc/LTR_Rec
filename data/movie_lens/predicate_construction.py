"""
This script constructs the necessary data files needed for psl-ltr-recommender
"""
import os
import pandas as pd
import numpy as np
import itertools
from sklearn.metrics.pairwise import cosine_similarity

"""
Import raw data
"""
movies_df = pd.read_csv('./ml-latest-small/movies.csv',
                        dtype={'movieId': str})
ratings_df = pd.read_csv('./ml-latest-small/ratings.csv',
                         dtype={'userId': str, 'movieId': str})
tags_df = pd.read_csv('./ml-latest-small/tags.csv',
                      dtype={'userId': str, 'movieId': str})

"""
Create data directory to write output to
"""
if not os.path.exists('./data'):
    os.makedirs('./data')

"""
Dev. subset to only 3 users and 100 movies
"""
n_users = 3
n_movies = 10
np.random.seed(0)

users = ratings_df.userId.unique()[:n_users]
ratings_df = ratings_df[ratings_df.userId.isin(users)]

movies = ratings_df.movieId.unique()
movies = np.random.choice(movies, n_movies, replace=False)
movies_df = movies_df[movies_df.movieId.isin(movies)]
ratings_df = ratings_df[ratings_df.movieId.isin(movies)]

tags_df = tags_df[tags_df.userId.isin(users) & tags_df.movieId.isin(movies)]

"""
Partition into target and observed movie ratings
"""
ratings_permutation = np.random.permutation(ratings_df.index)

observed_ratings = ratings_permutation[: int(3 * len(ratings_permutation) / 4)]
truth_ratings = ratings_permutation[int(3 * len(ratings_permutation) / 4):]

observed_ratings_frame = ratings_df.loc[observed_ratings]
truth_ratings_frame = ratings_df.loc[truth_ratings]

"""
User Similarity Predicate: built only from observed ratings
"""
user_rating_vectors = pd.DataFrame(data=0, index=users, columns=movies)
for u in users:
    user_ratings = observed_ratings_frame[observed_ratings_frame.userId == u]
    user_rating_vectors.loc[u, user_ratings.movieId.values] = user_ratings.rating.values

user_cosine_similarity_series = pd.DataFrame(data=cosine_similarity(user_rating_vectors),
                                             index=users,
                                             columns=users).stack()

user_cosine_similarity_series = user_cosine_similarity_series / user_cosine_similarity_series.max()

user_cosine_similarity_series.to_csv('./data/sim_users_obs.txt',
                                     sep='\t', header=False, index=True)

"""
Movie Similarity Predicate: built only from observed ratings
"""
movie_genres_series = movies_df.genres
movie_genres_series.index = movies_df.movieId
movie_genres_df = movie_genres_series.str.get_dummies('|')

movie_cosine_similarity_series = pd.DataFrame(data=cosine_similarity(movie_genres_df),
                                              index=movie_genres_df.index,
                                              columns=movie_genres_df.index).stack()

movie_cosine_similarity_series = movie_cosine_similarity_series / movie_cosine_similarity_series.max()

movie_cosine_similarity_series.to_csv('./data/sim_items_obs.txt',
                                      sep='\t', header=False, index=True)

"""
Preference Predicate
Re-Scaling ratings to 0-1 range
"""
# observed preference
observed_preference_frame = observed_ratings_frame.loc[:, ["userId", "movieId", "rating"]].set_index(["userId",
                                                                                                      "movieId"])
observed_preference_frame.rating = observed_preference_frame / observed_preference_frame.max()

# truth preference
truth_preference_frame = truth_ratings_frame.loc[:, ["userId", "movieId", "rating"]].set_index(["userId", "movieId"])
truth_preference_frame.rating = truth_preference_frame / truth_preference_frame.max()

# target preference
all_user_movie_index = pd.MultiIndex.from_product([users, movies])
all_preference_frame = pd.DataFrame(data=0, index=all_user_movie_index, columns=['rating'])
target_preference_frame = all_preference_frame.loc[~all_preference_frame.index.isin(observed_preference_frame.index), :]

observed_preference_frame.to_csv('./data/pref_obs.txt',
                                 sep='\t', header=False, index=True)
truth_preference_frame.to_csv('./data/pref_truth.txt',
                              sep='\t', header=False, index=True)
target_preference_frame.drop('rating', axis=1).to_csv('./data/pref_targets.txt',
                                                      sep='\t', header=False, index=True)

"""
Relative Rank Predicate
"""


def pairwise_comparison(rating_1, rating_2):
    if rating_1 == rating_2:
        return 0.5
    else:
        return np.heaviside(rating_1 - rating_2, 1)


# Method to return the user, movie1, movie2, relative rank tuple
def user_relative_ranks(ratings_frame):
    def func(u):
        user_ratings_df = ratings_frame[ratings_frame.userId == u].loc[:, ['movieId', 'rating']].set_index('movieId')
        user_pairwise_movies = itertools.product(user_ratings_df.index, user_ratings_df.index)
        return [(u,) +
                movie_pair +
                (pairwise_comparison(user_ratings_df.loc[movie_pair[0]].rating,
                                     user_ratings_df.loc[movie_pair[1]].rating), )
                for movie_pair in user_pairwise_movies]
    return func


# observed relative ranks
observed_user_movie_pairs = map(user_relative_ranks(observed_ratings_frame), users)
observed_user_movie_pairs = [item for sublist in observed_user_movie_pairs for item in sublist]
observed_relative_rank_df = pd.DataFrame(data=observed_user_movie_pairs,
                                         columns=['user', 'movie_1', 'movie_2', 'relative rank'])
observed_relative_rank_df = observed_relative_rank_df.set_index(['user', 'movie_1', 'movie_2'])

# truth relative ranks
truth_user_movie_pairs = map(user_relative_ranks(truth_ratings_frame), users)
truth_user_movie_pairs = [item for sublist in truth_user_movie_pairs for item in sublist]
truth_relative_rank_df = pd.DataFrame(data=truth_user_movie_pairs,
                                      columns=['user', 'movie_1', 'movie_2', 'relative rank'])
truth_relative_rank_df = truth_relative_rank_df.set_index(['user', 'movie_1', 'movie_2'])

# target relative ranks
all_user_movie_index = pd.MultiIndex.from_product([users, movies, movies])
all_relative_rank_df = pd.DataFrame(data=0, index=all_user_movie_index, columns=['relative rank'])
target_relative_rank_df = all_relative_rank_df.loc[~all_relative_rank_df.index.isin(observed_relative_rank_df.index)]

observed_relative_rank_df.to_csv('./data/rel_rank_obs.txt',
                                 sep='\t', header=False, index=True)
truth_relative_rank_df.to_csv('./data/rel_rank_truth.txt',
                              sep='\t', header=False, index=True)
target_relative_rank_df.drop('relative rank', axis=1).to_csv('./data/rel_rank_targets.txt',
                                                             sep='\t', header=False, index=True)
