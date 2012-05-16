import os
import pickle
from pytopic.pipeline.corpus import CorpusReader
from pytopic.pipeline.preprocess import filter_stopwords, filter_rarewords
from pytopic.util.data import load_stopwords
from pytopic.pipeline.reader import BibleTokenizer

DATASETS = '/home/jlund3/workspace/Datasets/'

STOP_ENGLISH = DATASETS + 'stopwords/english.txt'
STOP_BIBLE = DATASETS + 'stopwords/bible.txt'
STOP_NEWS = DATASETS + 'stopwords/newsgroup.txt'

NEWSGROUPS = DATASETS + '20ng/groups'
DELICIOUS = DATASETS + 'delicious'
BIBLE = DATASETS + 'bible/bible.txt'
TEST = DATASETS + 'test'

PICKLED = DATASETS + 'pickled'
PICKLED_NEWSGROUPS = PICKLED + '20ng.corpus'
PICKLED_DELICIOUS = PICKLED + 'delicious.corpus'
PICKLED_BIBLE = PICKLED + 'bible.corpus'

def get_20ng():
    if os.path.exists(PICKLED_NEWSGROUPS):
        return pickle.load(open(PICKLED_NEWSGROUPS))

    stopwords = load_stopwords(STOP_ENGLISH, STOP_NEWS)
    rare_threshold = 5

    reader = CorpusReader()
    reader.add_dir(NEWSGROUPS)
    corpus = reader.read()
    corpus = filter_stopwords(corpus, stopwords)
    corpus = filter_rarewords(corpus, rare_threshold)

    pickle.dump(corpus, open(PICKLED_NEWSGROUPS, 'w'))
    return corpus


def get_bible():
    if os.path.exists(PICKLED_BIBLE):
        return pickle.load(open(PICKLED_BIBLE))

    stopwords = load_stopwords(STOP_ENGLISH, STOP_BIBLE)
    rare_threshold = 10

    reader = CorpusReader(BibleTokenizer())
    reader.add_file(BIBLE)
    corpus = reader.read()
    corpus = filter_stopwords(corpus, stopwords)
    corpus = filter_rarewords(corpus, rare_threshold)

    pickle.dump(corpus, open(PICKLED_BIBLE, 'w'))
    return corpus


if __name__ == '__main__':
    get_20ng()
    get_bible()
