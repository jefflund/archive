import os
from scripts.get_data import get_20ng
from pytopic.topic.cluster import ClusterLDA
from pytopic.util.cluster import eval_hook

if __name__ == '__main__':
    pssh_node = int(os.environ.get('PSSH_NODENUM', '0'))

    corpus = get_20ng()
    K = 20
    T = 100
    gamma = 2
    alpha = .05
    beta = .001

    clda = ClusterLDA(corpus, K, T, gamma, alpha, beta)
    clda.output_hook = eval_hook(clda, corpus, 25)
    clda.inference(1000)
