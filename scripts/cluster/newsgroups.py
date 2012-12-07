import os
from scripts.dataset.newsgroups import get_corpus, get_clustering
from pytopic.topic.cluster import ClusterLDA
from pytopic.topic.mixmulti import MixtureMultinomial
from pytopic.util.handler import Timer, ClusterMetrics

# K T gamma alpha beta
clda_params = [[20, 100, 2, .05, .001],
               [20, 200, 2, .05, .001],
               [20, 500, 2, .05, .001]]

# K gamma beta
mm_params = [[20, 2, .1],
             [20, 2, .01],
             [20, 2, .001]]

# get 20 newsgropus dataset
corpus = get_corpus()
clustering = get_clustering(corpus)

# get run type from pssh
node_num = int(os.environ.get('PSSH_NODENUM', 0))
param_num = node_num // 2

# get model type and params
if node_num % 2 == 0:
    model_type = ClusterLDA
    params = clda_params[param_num]
else:
    model_type = MixtureMultinomial
    params = mm_params[param_num]

# construct the model
model = model_type(corpus, *params)
model.register_handler(Timer())
model.register_handler(ClusterMetrics(clustering, 10))

# print out run type
print model_type.__name__, params

# perform inference
model.inference(100)
