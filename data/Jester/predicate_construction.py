"""
This script constructs the necessary data files needed for psl-ltr-recommender
"""
import os
import pandas as pd
import numpy as np
import itertools
from sklearn.metrics.pairwise import cosine_similarity