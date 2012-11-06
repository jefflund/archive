from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.tokenizer import BibleTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords

def get_corpus():
    reader = CorpusReader(BibleTokenizer())
    reader.add_file('../data/bible/bible.txt')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/kj-english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus

if __name__ == '__main__':
    corpus = get_corpus()
