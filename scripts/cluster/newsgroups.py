from scripts.dataset.newsgroups import get_corpus, get_clustering
from pytopic.topic.cluster import ClusterLDA
from pytopic.util.handler import Timer, ClusterMetrics

def get_clda(corpus, clustering):
    K = 20
    T = 100
    gamma = 4
    alpha = .1
    beta = .001

    clda = ClusterLDA(corpus, K, T, gamma, alpha, beta)
    clda.register_handler(Timer())
    clda.register_handler(ClusterMetrics(clustering, 10))
    return clda

if __name__ == '__main__':
    corpus = get_corpus()
    clustering = get_clustering(corpus)
    clda = get_clda(corpus, clustering)
    clda.inference(100)
