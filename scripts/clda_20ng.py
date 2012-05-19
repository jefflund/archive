from __future__ import division

import os
from scripts.get_data import get_20ng
from pytopic.topic.cluster import ClusterLDA
from pytopic.util.cluster import eval_hook

if __name__ == '__main__':
    params = [1, 4, 5, 6, 7, 8, 9]

    pssh_node = int(os.environ.get('PSSH_NODENUM', '2'))
    param_set = pssh_node % len(params)

    temp = params[param_set]
    print temp

    corpus = get_20ng()
    K = 20
    T = 100
    gamma = 2
    alpha = .05
    beta = .001

    clda = ClusterLDA(corpus, K, T, gamma, alpha, beta)
    clda.output_hook = eval_hook(clda, corpus, 25)

    clda.set_anneal_temp(1 / temp)
    clda.inference(1000)

    clda.set_anneal_temp(1)
    clda.inference(1000)
