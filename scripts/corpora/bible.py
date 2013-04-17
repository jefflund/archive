from pytopic.pipeline import dataset, tokenizer, preprocess
from pytopic.analysis import xref
from pytopic.util import data

@data.pickle_cache('../pickle/bible-corpus.pickle')
def get_corpus():
    reader = dataset.CorpusReader(tokenizer.BibleTokenizer())
    reader.add_file('../data/bible/bible.txt')
    corpus = reader.read()

    stopwords = preprocess.load_stopwords('../data/stopwords/english.txt',
                                          '../data/stopwords/king-james.txt')
    corpus = preprocess.filter_stopwords(corpus, stopwords, retain_empty=True)

    return corpus


@data.pickle_cache('../pickle/bible-xrefs.pickle')
def get_xrefs(corpus):
    reader = xref.XRefReader(corpus)
    reader.add_file('../data/bible/xref.txt')
    return reader.read()


@data.pickle_cache('../pickle/bible-concord.pickle')
def get_concordance(corpus):
    return xref.Concordance(corpus)
