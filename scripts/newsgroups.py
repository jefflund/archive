from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.topic.vanilla import VanillaLDA
from pytopic.util.handler import Printer, Timer
from pytopic.util.data import pickle_cache

@pickle_cache('pickle/newsgroups.corpus.pickle')
def get_corpus():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    stopwords.add('writes')
    stopwords.add('article')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

def get_model(corpus):
    lda = VanillaLDA(corpus, 20, .4, .01)
    lda.register_handler(Printer(10))
    lda.register_handler(Timer())
    return lda

if __name__ == '__main__':
    corpus = get_corpus()
    lda = get_model(corpus)
    lda.inference(100)
