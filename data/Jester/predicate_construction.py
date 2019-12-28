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
ratings_obs_df = pd.read_csv('./jester/0/eval/rating_obs.txt', sep='\t', header=None)
ratings_obs_df.columns = ['userId', 'jokeId', 'rating']
ratings_obs_df = ratings_obs_df.astype({'userId': str, 'jokeId': str, 'rating': float})

ratings_targets_df = pd.read_csv('./jester/0/eval/rating_targets.txt', sep='\t', header=None)
ratings_targets_df.columns = ['userId', 'jokeId']
ratings_targets_df = ratings_targets_df.astype({'userId': str, 'jokeId': float})

ratings_truth_df = pd.read_csv('./jester/0/eval/rating_truth.txt', sep='\t', header=None)
ratings_truth_df.columns = ['userId', 'jokeId', 'rating']
ratings_truth_df = ratings_truth_df.astype({'userId': str, 'jokeId': str, 'rating': float})

users = ratings_obs_df.userId.unique()
jokes = ratings_obs_df.jokeId.unique()

"""
Relative Rank
"""


# Method to return the user, movie1, movie2, relative rank tuple
def user_relative_ranks(ratings_frame):
    def func(u):
        user_ratings_df = ratings_frame[ratings_frame.userId == u].loc[:, ['jokeId', 'rating']].set_index('jokeId')
        user_pairwise_jokes = np.subtract.outer(user_ratings_df.rating.to_numpy(), user_ratings_df.rating.to_numpy())
        binary_user_pairwise_jokes = user_pairwise_jokes.copy()
        binary_user_pairwise_jokes[user_pairwise_jokes < 0] = 0
        binary_user_pairwise_jokes[user_pairwise_jokes > 0] = 1
        binary_user_pairwise_jokes[user_pairwise_jokes == 0] = 0.5
        binary_user_pairwise_jokes_df = pd.DataFrame(binary_user_pairwise_jokes,
                                                     columns=user_ratings_df.index,
                                                     index=user_ratings_df.index).stack()
        binary_user_pairwise_jokes_df.name = u
        return binary_user_pairwise_jokes_df
    return func


# observed relative ranks
observed_user_joke_pairs = list(map(user_relative_ranks(ratings_obs_df), users))
observed_relative_rank_df = pd.concat(observed_user_joke_pairs, keys=[df.name for df in observed_user_joke_pairs])

# truth relative ranks
truth_user_joke_pairs = map(user_relative_ranks(ratings_truth_df), users)
truth_relative_rank_df = pd.concat(observed_user_joke_pairs, keys=[df.name for df in observed_user_joke_pairs])

# target relative rank
all_user_joke_index = pd.MultiIndex.from_product([users, jokes, jokes])
all_relative_rank_df = pd.DataFrame(index=all_user_joke_index)
target_relative_rank_df = all_relative_rank_df.loc[~all_relative_rank_df.index.isin(observed_relative_rank_df.index)]

observed_relative_rank_df.to_csv('./jester/0/eval/rel_rank_obs.txt',
                                 sep='\t', header=False, index=True)
truth_relative_rank_df.to_csv('./jester/0/eval/rel_rank_truth.txt',
                              sep='\t', header=False, index=True)
target_relative_rank_df.to_csv('./jester/0/eval/rel_rank_targets.txt',
                               sep='\t', header=False, index=True)
