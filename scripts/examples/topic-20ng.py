from pytopic.pipeline import dataset, tokenizer, preprocess
from pytopic.model import vanilla
from pytopic.util import handler
from pytopic.util.data import pickle_cache

@pickle_cache('../pickle/newsgroups-corpus-reduced.pickle')
def get_corpus():
    reader = dataset.CorpusReader(tokenizer.NewsTokenizer())
    reader.add_index_dir('../data/newsgroups/indices/reduced_set/all',
                         '../data/newsgroups')
    corpus = reader.read()

    stopwords = preprocess.load_stopwords('../data/stopwords/english.txt',
                                          '../data/stopwords/newsgroups.txt')
    corpus = preprocess.filter_stopwords(corpus, stopwords)

    return corpus

def get_model(corpus):
    lda = vanilla.VanillaLDA(corpus, 20, .4, .01)
    lda.register_handler(handler.Timer())
    lda.register_handler(handler.Printer(10))
    return lda

if __name__ == '__main__':
    corpus = get_corpus()
    lda = get_model(corpus)
    lda.inference(100)
