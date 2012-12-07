import os
from scripts.dataset.newsgroups import get_corpus, get_clustering
from pytopic.topic.cluster import ClusterLDA
from pytopic.topic.mixmulti import MixtureMultinomial
from pytopic.util.handler import Timer, ClusterMetrics

clda_params = [[20, 100, 2, .05, .001],
               [20, 200, 2, .05, .001],
               [20, 500, 2, .05, .001]]

mm_params = [[20, 2, .1],
             [20, 2, .01],
             [20, 2, .001]]

def get_model(corpus, clustering):
    node_num = int(os.environ.get('PSSH_NODENUM', 0))
    param_num = node_num // 2

    if node_num % 2 == 0:
        model_type = ClusterLDA
        params = clda_params[param_num]
    else:
        model_type = MixtureMultinomial
        params = mm_params[param_num]

    model = model_type(corpus, *params)
    model.register_handler(Timer())
    model.register_handler(ClusterMetrics(clustering, 10))

    return model, params

if __name__ == '__main__':
    corpus = get_corpus()
    clustering = get_clustering(corpus)

    model, params = get_model(corpus, clustering)
    print model.__class__.__name__, params

    model.inference(100)
