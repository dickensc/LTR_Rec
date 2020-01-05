"""
Helper methods for predicate construction
"""

import pandas as pd
import numpy as np

from sklearn.cluster import AgglomerativeClustering
from sklearn.metrics.pairwise import cosine_similarity


def target_preferences(item_canopy, queries):
    """

    :param item_canopy:
    :param queries:
    :return:
    """
    filtered_item_item_index = item_canopy[item_canopy == 1].index
    target_preference_series = pd.DataFrame(index=filtered_item_item_index, columns=queries).stack(dropna=False)
    target_preference_series = target_preference_series.swaplevel(2, 0)
    return target_preference_series


def filter_and_write_targets(target_preference_series, observed_preferences_df, write_path):
    """

    :param target_preference_series:
    :param observed_preferences_df:
    :param write_path:
    :return:
    """
    # filter and write targets in chunks
    n = 1000000
    write_mode = 'w'  # first overwrite then append
    for target_chunk in [target_preference_series[i:i + n] for i in range(0, target_preference_series.shape[0], n)]:
        target_preferences_tuples = set(target_chunk.index.to_numpy()).difference(
            set(observed_preferences_df.index.to_numpy())
        )

        filtered_target_preferences_index = pd.MultiIndex.from_tuples(target_preferences_tuples)
        target_chunk_series = pd.DataFrame(index=filtered_target_preferences_index)

        target_chunk_series.to_csv(write_path, sep='\t', header=False, index=True, mode=write_mode)
        write_mode = 'a'


def hac_canopy_from_distance(distance_series, distance_threshold=None, n_clusters=None):
    """

    :param distance_series:
    :param distance_threshold:
    :param n_clusters:
    :return:
    """
    distance_df = distance_series.unstack().fillna(1)
    clustering = AgglomerativeClustering(affinity='precomputed', linkage='average',
                                         distance_threshold=distance_threshold,
                                         n_clusters=n_clusters)
    clustering = clustering.fit(distance_df)

    # Two users are in the same canopy if they have the same cluster label
    canopy_series = pd.Series(data=0, index=distance_series.index)

    print(clustering.n_clusters_)

    for cluster_label in range(clustering.n_clusters_):
        cluster_member_boolean_array = clustering.labels_ == cluster_label
        cluster_member_id_array = distance_df.index[cluster_member_boolean_array]
        bool_canopy_indexer = (canopy_series.index.get_level_values(0).isin(cluster_member_id_array)
                               & canopy_series.index.get_level_values(1).isin(cluster_member_id_array))
        canopy_series[bool_canopy_indexer] = 1

    return canopy_series


def query_relevance_cosine_similarity(relevance_df, query_index, item_index, relevance_index):
    """
    Builds query similarity predicate from a ratings data frame.

    Note: In this implementation we are considering the union of relevance values between queries, so if the
    relevance score is missing for one query, it is assumed to be 0 and considered in similarity calculation.
    We may want to first find the intersection of existing relevance items, then use those to calculate similarity.

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