from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache

@pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_newsgroups():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/newsgroups.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


@pickle_cache('../pickle/enron-corpus.pickle')
def get_enron():
    reader = CorpusReader(NewsTokenizer())
    reader.add_index_dir('../data/enron/indices/ldc_split/all',
                         '../data/enron')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus
