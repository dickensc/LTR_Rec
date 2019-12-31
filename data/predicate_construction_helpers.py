"""
Helper methods for predicate construction
"""

import pandas as pd
import numpy as np

from sklearn.metrics.pairwise import cosine_similarity


def query_cosine_similarity(relevance_df, query_index, item_index, relevance_index):
    """
    Builds query similarity predicate from a ratings data frame
    :param relevance_df: A dataframe with a query, item and relevance column fields
    :param query_index: name of query field
    :param item_index: name of item field
    :param relevance_index: name of relevance field
    :return: multi index (query_id, item_id) Series
    """
    query_ids = relevance_df[query_index].unique()
    item_ids = relevance_df[item_index].unique()

    query_relevance_vectors = pd.DataFrame(data=0, index=query_ids, columns=item_ids)
    for q in query_ids:
        query_relevance = relevance_df[relevance_df[query_index] == q]
        query_relevance_vectors.loc[q, query_relevance[item_index].values] = query_relevance[relevance_index].values

    query_cosine_similarity_series = pd.DataFrame(data=cosine_similarity(query_relevance_vectors),
                                                  index=query_ids,
                                                  columns=query_ids).stack()

    query_cosine_similarity_series = query_cosine_similarity_series / query_cosine_similarity_series.max()

    return query_cosine_similarity_series


def query_item_preferences(ratings_frame, query_index, item_index, relevance_index):
    """
    Method to return the query, item1, item2, relative rank tuple
    :param relevance_index:
    :param item_index:
    :param query_index:
    :param ratings_frame:
    :return:
    """
    def func(query):
        query_ratings_df = ratings_frame[ratings_frame[query_index] == query].loc[:,
                           [item_index, relevance_index]].set_index(item_index)
        query_pairwise_item_preference = np.subtract.outer(query_ratings_df.rating.to_numpy(),
                                                           query_ratings_df.rating.to_numpy())
        query_pairwise_item_preference_df = pd.DataFrame(query_pairwise_item_preference,
                                                         index=query_ratings_df.index,
                                                         columns=query_ratings_df.index)
        binary_query_item_preference_df = query_pairwise_item_preference_df.copy()
        binary_query_item_preference_df[query_pairwise_item_preference_df < 0] = 0
        binary_query_item_preference_df[query_pairwise_item_preference_df > 0] = 1
        binary_query_item_preference_df[query_pairwise_item_preference_df == 0] = 0.5
        binary_query_item_preference_series = binary_query_item_preference_df.stack()
        binary_query_item_preference_series.name = query
        return binary_query_item_preference_series

    return func