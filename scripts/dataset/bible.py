from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import BibleTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.analysis.xref import XRefReader, Concordance
from pytopic.util.data import pickle_cache

@pickle_cache('pickle/bible/corpus.pickle')
def get_corpus():
    reader = CorpusReader(BibleTokenizer())
    reader.add_file('../data/bible/bible.txt')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/kj-english.txt')
    corpus = filter_stopwords(corpus, stopwords, retain_empty=True)

    return corpus

@pickle_cache('pickle/bible/xrefs.pickle')
def get_xrefs(corpus):
    reader = XRefReader(corpus)
    reader.add_file('../data/bible/xref.txt')
    return reader.read()

@pickle_cache('pickle/bible/concord.pickle')
def get_concordance(corpus):
    return Concordance(corpus)

if __name__ == '__main__':
    corpus = get_corpus()
    xrefs = get_xrefs(corpus)
    concord = get_concordance(corpus)
