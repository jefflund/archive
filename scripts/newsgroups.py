from pytopic.pipeline.corpus import Corpus
from pytopic.pipeline.tokenizer import NewsReader
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache
from pytopic.model.vanilla import VanillaLDA
from pytopic.util.data import Printer, Timer()

@pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_corpus():
    reader = CorpusReader(NewsReader())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    stopwords.add('writes')
    stopwords.add('article')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

def get_model(corpus):
    lda = VanillaLDA(20, .4, .01)
    lda.register_handler(Timer())
    lda.register_handler(Printer(10))
    return lda

if __name__ == '__main__':
    corpus = get_corpus()
    lda = get_model()
    lda.inference(100)
