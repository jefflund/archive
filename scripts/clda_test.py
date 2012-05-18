from __future__ import division

from pytopic.topic.cluster import ClusterLDA
from pytopic.util.cluster import eval_hook
from scripts.get_data import get_test
import os


if __name__ == '__main__':
    pssh_node = int(os.environ.get('PSSH_NODENUM', '0'))

    corpus = get_test()
    K = 2
    T = 20
    gamma = 2
    alpha = .05
    beta = .001

    clda = ClusterLDA(corpus, K, T, gamma, alpha, beta)
    clda.output_hook = eval_hook(clda, corpus, 25)
    clda.set_anneal_temp(.9)
    clda.inference(50)
    clda.set_anneal_temp(1)
    clda.inference(50)
