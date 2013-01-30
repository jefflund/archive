from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache
from pytopic.model.mixmulti import MixtureMultinomial
from pytopic.util.handler import Printer, Timer, ClusterMetrics
from pytopic.analysis.cluster import Clustering

@pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_corpus():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/newsgroups.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

def get_model(corpus):
    mm = MixtureMultinomial(corpus, 20, 2, 2)
    mm.set_inference('em')
    mm.register_handler(Timer())
    mm.register_handler(Printer(10))
    mm.register_handler(ClusterMetrics(Clustering.from_corpus(corpus), 10))
    return mm

if __name__ == '__main__':
    corpus = get_corpus()
    mm = get_model(corpus)
    mm.inference(100)
