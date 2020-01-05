"""
This script constructs the necessary data files needed for psl-ltr-recommender
"""
import os
import pandas as pd
import numpy as np

import sys
sys.path.append('../')

from predicate_construction_helpers import query_relevance_cosine_similarity
from predicate_construction_helpers import query_item_preferences
from predicate_construction_helpers import hac_canopy_from_distance
from predicate_construction_helpers import target_preferences
from predicate_construction_helpers import filter_and_write_targets
from sklearn.metrics.pairwise import cosine_similarity

"""
Import raw data
"""
movies_df = pd.read_csv('./ml-100k/u.item', sep='|', header=None, encoding="ISO-8859-1")
movies_df.columns = ["movieId", "movie title", "release date", "video release date", "IMDb URL ", "unknown", "Action",
                     "Adventure", "Animation", "Children's", "Comedy", "Crime", "Documentary", "Drama", "Fantasy",
                     "Film-Noir", "Horror", "Musical", "Mystery", "Romance", "Sci-Fi", "Thriller", "War", "Western"]
movies_df = movies_df.astype({'movieId': str})

ratings_df = pd.read_csv('./ml-100k/u.data', sep='\t', header=None)
ratings_df.columns = ['userId', 'movieId', 'rating', 'timestamp']
ratings_df = ratings_df.astype({'userId': str, 'movieId': str})

users = ratings_df.userId.unique()
movies = ratings_df.movieId.unique()

print("Num Users: {}".format(users.shape[0]))
print("Num Movies: {}".format(movies.shape[0]))

"""
Create data directory to write output to
"""
if not os.path.exists('./movie_lens'):
    os.makedirs('./movie_lens')

"""
Dev. subset to only 10 users and 100 movies
"""
n_users = 3
n_movies = 200
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
user_cosine_similarity_series = query_relevance_cosine_similarity(observed_ratings_frame, 'userId', 'movieId', 'rating')
print("User Similarity Memory Usage {}".format(user_cosine_similarity_series.memory_usage()))
user_cosine_similarity_series.to_csv('./movie_lens/sim_users_obs.txt',
                                     sep='\t', header=False, index=True)

"""
User User Canopy
"""

user_cosine_distance_series = 1 - user_cosine_similarity_series
user_user_canopy_series = hac_canopy_from_distance(user_cosine_distance_series, user_cosine_distance_series.mean())
user_user_canopy_series.to_csv('./movie_lens/user_user_canopy_obs.txt', sep='\t', header=False, index=True)

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

print("Movie Similarity Memory Usage {}".format(movie_cosine_similarity_series.memory_usage()))

movie_cosine_similarity_series.to_csv('./movie_lens/sim_items_obs.txt', sep='\t', header=False, index=True)

"""
Movie Movie Canopy
"""

movie_cosine_distance_series = (1 - movie_cosine_similarity_series)
movie_movie_canopy_series = hac_canopy_from_distance(movie_cosine_distance_series, n_clusters=int(movies.shape[0]/4))
movie_movie_canopy_series.to_csv('./movie_lens/movie_movie_canopy_obs.txt', sep='\t', header=False, index=True)

"""
Rating Predicate
Re-Scaling ratings to 0-1 range
"""
# observed preference
obs_rating_df = observed_ratings_frame.loc[:, ["userId", "movieId", "rating"]].set_index(["userId", "movieId"])
obs_rating_df.rating = obs_rating_df / obs_rating_df.max()

# truth preference
truth_rating_df = truth_ratings_frame.loc[:, ["userId", "movieId", "rating"]].set_index(["userId", "movieId"])
truth_rating_df.rating = truth_rating_df / truth_rating_df.max()

# target preference
all_user_movie_index = pd.MultiIndex.from_product([users, movies])
all_preference_frame = pd.DataFrame(data=0, index=all_user_movie_index, columns=['rating'])
target_rating_df = all_preference_frame.loc[~all_preference_frame.index.isin(obs_rating_df.index), :]

obs_rating_df.to_csv('./movie_lens/rating_obs.txt',
                     sep='\t', header=False, index=True)
truth_rating_df.to_csv('./movie_lens/rating_truth.txt',
                       sep='\t', header=False, index=True)
target_rating_df.drop('rating', axis=1).to_csv('./movie_lens/rating_targets.txt',
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


observed_relative_rank_df.to_csv('./movie_lens/rel_rank_obs.txt',
                                 sep='\t', header=False, index=True,
                                 chunksize=100000)

# truth relative ranks
truth_user_movie_preferences = list(
    map(query_item_preferences(truth_ratings_frame, 'userId', 'movieId', 'rating'),
        truth_ratings_frame.userId.unique()
        )
)
truth_relative_rank_df = pd.concat(truth_user_movie_preferences, keys=[df.name for df in truth_user_movie_preferences])

truth_relative_rank_df.to_csv('./movie_lens/rel_rank_truth.txt',
                              sep='\t', header=False, index=True,
                              chunksize=100000)

# target relative rank
target_relative_rank_series = target_preferences(movie_movie_canopy_series, users)

write_path = './movie_lens/rel_rank_targets.txt'
filter_and_write_targets(target_relative_rank_series, observed_relative_rank_df, write_path)
