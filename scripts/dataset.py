from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer, BibleTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.analysis.cluster import Clustering
from pytopic.analysis.xref import XRefReader, Concordance
from pytopic.util.data import pickle_cache

# 20 Newsgroups

@pickle_cache('pickle/newsgroups/corpus.pickle')
def get_newsgroups_corpus():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    stopwords.add('writes')
    stopwords.add('article')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

@pickle_cache('pickle/newsgroups/clustering.pickle')
def get_newsgroups_clustering(corpus):
    return Clustering.from_corpus(corpus)

# Bible

@pickle_cache('pickle/bible/corpus.pickle')
def get_bible_corpus():
    reader = CorpusReader(BibleTokenizer())
    reader.add_file('../data/bible/bible.txt')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/kj-english.txt')
    corpus = filter_stopwords(corpus, stopwords, retain_empty=True)

    return corpus

@pickle_cache('pickle/bible/xrefs.pickle')
def get_bible_xrefs(corpus):
    reader = XRefReader(corpus)
    reader.add_file('../data/bible/xref.txt')
    return reader.read()

@pickle_cache('pickle/bible/concord.pickle')
def get_bible_concordance(corpus):
    return Concordance(corpus)
