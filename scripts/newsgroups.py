from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import NewsTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.topic.vanilla import VanillaLDA
import time

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
    return VanillaLDA(corpus, 20, .4, .01)


if __name__ == '__main__':
    start = time.time()
    corpus = get_corpus()
    loaded = time.time()
    lda = get_model(corpus)
    ready = time.time()
    lda.inference(10)
    done = time.time()
    lda.print_state()
    print 'loaded:', loaded - start
    print 'ready:', ready - loaded
    print 'done:', done - ready
    print 'total:', done - start
