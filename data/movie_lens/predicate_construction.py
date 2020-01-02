"""
This script constructs the necessary data files needed for psl-ltr-recommender
"""
import os
import pandas as pd
import numpy as np

import sys
sys.path.append('../')
from predicate_construction_helpers import query_cosine_similarity
from predicate_construction_helpers import query_item_preferences
from sklearn.metrics.pairwise import cosine_similarity

"""
Import raw data
"""
movies_df = pd.read_csv('./ml-100k/u.item', sep='|', header=None, encoding="ISO-8859-1")
movies_df.columns = ["movieId", "movie title", "release date", "video release date", "IMDb URL ", "unknown", "Action",
                     "Adventure", "Animation", "Children's", "Comedy", "Crime", "Documentary", "Drama", "Fantasy",
                     "Film-Noir", "Horror", "Musical", "Mystery", "Romance", "Sci-Fi", "Thriller", "War", "Western"]
movies_df=movies_df.astype({'movieId': str})

ratings_df = pd.read_csv('./ml-100k/u.data', sep='\t', header=None)
ratings_df.columns = ['userId', 'movieId', 'rating', 'timestamp']
ratings_df = ratings_df.astype({'userId': str, 'movieId': str})

"""
Create data directory to write output to
"""
if not os.path.exists('./data'):
    os.makedirs('./data')

"""
Dev. subset to only 10 users and 100 movies
"""
n_users = 10
n_movies = 100
np.random.seed(0)

users = ratings_df.userId.unique()[:n_users]
ratings_df = ratings_df[ratings_df.userId.isin(users)]

movies = ratings_df.movieId.unique()
movies = np.random.choice(movies, n_movies, replace=False)
movies_df = movies_df[movies_df.movieId.isin(movies)]
ratings_df = ratings_df[ratings_df.movieId.isin(movies)]

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
user_cosine_similarity_series = query_cosine_similarity(observed_ratings_frame, 'userId', 'movieId', 'rating')
user_cosine_similarity_series.to_csv('./data/sim_users_obs.txt',
                                     sep='\t', header=False, index=True)

"""
Movie Similarity Predicate: built from genres
"""
movie_genres_df = movies_df.loc[:, ["unknown", "Action", "Adventure", "Animation", "Children's", "Comedy", "Crime",
                                    "Documentary", "Drama", "Fantasy", "Film-Noir", "Horror", "Musical", "Mystery",
                                    "Romance", "Sci-Fi", "Thriller", "War", "Western"]]
movie_genres_df.index = movies_df.movieId

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
# observed relative ranks
observed_user_movie_preferences = list(
    map(query_item_preferences(observed_ratings_frame, 'userId', 'movieId', 'rating'),
        observed_ratings_frame.userId.unique()
        )
)
observed_relative_rank_df = pd.concat(observed_user_movie_preferences, keys=[df.name for df in
                                                                            observed_user_movie_preferences])

# truth relative ranks
truth_user_movie_preferences = list(
    map(query_item_preferences(truth_ratings_frame, 'userId', 'movieId', 'rating'),
        truth_ratings_frame.userId.unique()
        )
)
truth_relative_rank_df = pd.concat(truth_user_movie_preferences, keys=[df.name for df in
                                                                      truth_user_movie_preferences])

# target relative rank
all_user_movie_index = pd.MultiIndex.from_product([users, movies, movies])
all_relative_rank_df = pd.DataFrame(index=all_user_movie_index)
target_relative_rank_df = all_relative_rank_df.loc[
    ~all_relative_rank_df.index.isin(observed_relative_rank_df.index)]


observed_relative_rank_df.to_csv('./data/rel_rank_obs.txt',
                                 sep='\t', header=False, index=True)
truth_relative_rank_df.to_csv('./data/rel_rank_truth.txt',
                              sep='\t', header=False, index=True)
target_relative_rank_df.to_csv('./data/rel_rank_targets.txt',
                               sep='\t', header=False, index=True)
