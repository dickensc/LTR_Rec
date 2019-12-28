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
ratings_df = pd.read_csv('./jester/0/eval/ratings.csv',
                         dtype={'userId': str, 'movieId': str})
