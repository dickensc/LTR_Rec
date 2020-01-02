"""
This script constructs the necessary data files needed for psl-ltr-recommender
"""
import pandas as pd

import sys
sys.path.append('../')
from predicate_construction_helpers import query_item_preferences
"""
Jester Configs
"""
data_path = './yelp'
dataset_directory_nums = ['0', '1', '2', '3', '4']
dataset_types = ['eval', 'learn']

"""
iterate over all dataset directories and types
"""

for data_dir_num in dataset_directory_nums:
    for data_type in dataset_types:
        """
        Import raw data
        """
        ratings_obs_df = pd.read_csv(data_path + '/' + data_dir_num + '/' + data_type + '/' + 'rating_obs.txt',
                                     sep='\t', header=None)
        ratings_obs_df.columns = ['userId', 'itemId', 'rating']
        ratings_obs_df = ratings_obs_df.astype({'userId': str, 'itemId': str, 'rating': float})

        ratings_targets_df = pd.read_csv(data_path + '/' + data_dir_num + '/' + data_type + '/' + 'rating_targets.txt',
                                         sep='\t', header=None)
        ratings_targets_df.columns = ['userId', 'itemId']
        ratings_targets_df = ratings_targets_df.astype({'userId': str, 'itemId': float})

        ratings_truth_df = pd.read_csv(data_path + '/' + data_dir_num + '/' + data_type + '/' + 'rating_truth.txt',
                                       sep='\t', header=None)
        ratings_truth_df.columns = ['userId', 'itemId', 'rating']
        ratings_truth_df = ratings_truth_df.astype({'userId': str, 'itemId': str, 'rating': float})

        """
        Relative Rank
        """
        # observed relative ranks
        observed_user_joke_preferences = list(map(
            query_item_preferences(ratings_obs_df, 'userId', 'itemId', 'rating'), ratings_obs_df.userId.unique()
        ))
        observed_relative_rank_df = pd.concat(observed_user_joke_preferences, keys=[df.name for df in
                                                                                    observed_user_joke_preferences])

        # truth relative ranks
        truth_user_joke_preferences = list(map(
            query_item_preferences(ratings_truth_df, 'userId', 'itemId', 'rating'), ratings_truth_df.userId.unique()
        ))
        truth_relative_rank_df = pd.concat(truth_user_joke_preferences, keys=[df.name for df in
                                                                              truth_user_joke_preferences])

        # target relative rank
        target_users = ratings_targets_df.userId.unique()
        target_jokes = ratings_targets_df.jokeId.unique()
        all_user_joke_index = pd.MultiIndex.from_product([target_users, target_jokes, target_jokes])
        all_relative_rank_df = pd.DataFrame(index=all_user_joke_index)
        target_relative_rank_df = all_relative_rank_df.loc[
            ~all_relative_rank_df.index.isin(observed_relative_rank_df.index)]

        observed_relative_rank_df.to_csv(data_path + '/' + data_dir_num + '/' + data_type + '/' + 'rel_rank_obs.txt',
                                         sep='\t', header=False, index=True)
        truth_relative_rank_df.to_csv(data_path + '/' + data_dir_num + '/' + data_type + '/' + 'rel_rank_truth.txt',
                                      sep='\t', header=False, index=True)
        target_relative_rank_df.to_csv(data_path + '/' + data_dir_num + '/' + data_type + '/' + 'rel_rank_targets.txt',
                                       sep='\t', header=False, index=True)
