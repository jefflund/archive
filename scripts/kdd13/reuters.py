#!/usr/bin/pypy

import collections
import sgmllib
from pytopic.pipeline.corpus import CorpusReader, Tokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache
from pytopic.analysis.cluster import Clustering
from scripts.kdd13.runner import main


class ReutersTokenizer(Tokenizer, sgmllib.SGMLParser):

    def __init__(self, topic_min, split_re=None, filter_re=None):
        Tokenizer.__init__(self, split_re, filter_re)
        sgmllib.SGMLParser.__init__(self)


        self.topic_min = topic_min

        self.reset_parse()
        self.output = []

    def tokenize(self, filename, buff):
        self.parse(buff)

        counts = collections.Counter()
        for _, _, topics in self.output:
            counts.update(topics)
        all_topics = {t for t, c in counts.iteritems() if c > self.topic_min}

        for title, text, topics in self.output:
            for topic in topics:
                if topic in all_topics:
                    tokens = self.split_re.split(text)
                    tokens = [self.transform(token) for token in tokens]
                    tokens = [token for token in tokens if self.keep(token)]
                    yield title, tokens
                    break

    def get_clusters(self, filename):
        self.parse(open(filename))

        for title, _, topics in self.output:
            yield title, topics

    def parse(self, buff):
        self.output = []
        self.feed(buff.read())
        self.close()

    def reset_parse(self):
        self.curr_docid = None
        self.curr_text = []
        self.curr_topics = []
        self.reading_text = False
        self.reading_topic = False

    def handle_data(self, data):
        if self.reading_text:
            self.curr_text.append(data)
        elif self.reading_topic:
            self.curr_topics.append(data)

    def start_reuters(self, attrs):
        attrs = dict(attrs)
        self.curr_docid = attrs['newid']

    def start_topics(self, attrs):
        self.reading_topic = True

    def end_topics(self):
        self.reading_topic = False

    def start_title(self, attrs):
        self.reading_text = True

    def end_title(self):
        self.reading_text = False

    def start_body(self, attrs):
        self.reading_text = True

    def end_body(self):
        self.reading_text = False

    def end_reuters(self):
        if len(self.curr_topics) == 1:
            text = '\n'.join(self.curr_text)
            topics = tuple(self.curr_topics)
            output = self.curr_docid, text, topics
            self.output.append(output)

        self.reset_parse()


MIN_CLUSTER_SIZE = 20


def cluster_reuters(corpus):
    topics = {}

    tokenizer = ReutersTokenizer(MIN_CLUSTER_SIZE)
    for fileid in range(22):
        fileid = str(fileid).rjust(3, '0')
        filename = '../data/reuters/reut2-{}.sgm'.format(fileid)
        for title, cluster in tokenizer.get_clusters(filename):
            topics[title] = cluster[0]

    data = [topics[corpus.titles[d]] for d in range(len(corpus))]
    labels = set(data)
    return Clustering(labels, data)


#@pickle_cache('../pickle/reuters-corpus.pickle')
def get_reuters():
    reader = CorpusReader(ReutersTokenizer(MIN_CLUSTER_SIZE))
    for fileid in range(22):
        fileid = str(fileid).rjust(3, '0')
        reader.add_file('../data/reuters/reut2-{}.sgm'.format(fileid))
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


main('Reuters', get_reuters, cluster_reuters)
