import sgmllib
from pytopic.pipeline.corpus import CorpusReader, Tokenizer
from pytopic.pipeline.tokenizer import NewsTokenizer, HTMLTokenizer
from pytopic.pipeline.preprocess import load_stopwords, filter_stopwords
from pytopic.util.data import pickle_cache

@pickle_cache('../pickle/newsgroups-corpus.pickle')
def get_newsgroups():
    reader = CorpusReader(NewsTokenizer())
    reader.add_dir('../data/newsgroups/groups')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt',
                               '../data/stopwords/newsgroups.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


@pickle_cache('../pickle/enron-corpus.pickle')
def get_enron():
    reader = CorpusReader(NewsTokenizer())
    reader.add_index_dir('../data/enron/indices/ldc_split/all',
                         '../data/enron')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


class WebKBTokenizer(HTMLTokenizer):

    def __init__(self, split_re='\s+', filter_re='[^a-zA-Z]'):
        HTMLTokenizer.__init__(self, split_re, filter_re)

    def tokenize(self, filename, buff):
        line = buff.readline()
        while line.strip() != '':
            line = buff.readline()
        return HTMLTokenizer.tokenize(self, filename, buff)


@pickle_cache('../pickle/webkb-corpus.pickle')
def get_webkb():
    reader = CorpusReader(WebKBTokenizer())
    reader.add_dir('../data/webkb')
    corpus = reader.read()

    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus


class ReutersTokenizer(Tokenizer, sgmllib.SGMLParser):

    def __init__(self, split_re='\s+', filter_re='[^a-zA_Z]'):
        Tokenizer.__init__(self, split_re, filter_re)
        sgmllib.SGMLParser.__init__(self)

        self.reset_parse()
        self.output = []

    def tokenize(self, filename, buff):
        self.output = []
        self.feed(buff.read())
        self.close()

        for title, text in self.output:
            tokens = self.split_re.split(text)
            tokens = [self.transform(token) for token in tokens]
            tokens = [token for token in tokens if self.keep(token)]
            yield title, tokens

    def reset_parse(self):
        self.curr_docid = None
        self.curr_text = []
        self.reading = False

    def start_reuters(self, attrs):
        attrs = dict(attrs)
        if attrs['topics'] == 'YES':
            self.curr_docid = attrs['newid']

    def start_title(self, attrs):
        self.reading = True

    def end_title(self):
        self.reading = False

    def start_body(self, attrs):
        self.reading = True

    def end_body(self):
        self.reading = False

    def handle_data(self, data):
        if self.reading:
            self.curr_text.append(data)

    def end_reuters(self):
        if self.curr_docid is not None:
            doc = self.curr_docid, '\n'.join(self.curr_text)
            self.output.append(doc)

        self.reset_parse()


@pickle_cache('../pickle/reuters-corpus.pickle')
def get_reuters():
    reader = CorpusReader(ReutersTokenizer())
    for fileid in range(22):
        fileid = str(fileid).rjust(3, '0')
        reader.add_file('../data/reuters/reut2-{}.sgm'.format(fileid))
    corpus = reader.read()


    stopwords = load_stopwords('../data/stopwords/english.txt')
    corpus = filter_stopwords(corpus, stopwords)

    return corpus
