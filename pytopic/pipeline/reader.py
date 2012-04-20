"""Special corpora-specific corpus building tools"""

from pytopic.pipeline.corpus import Tokenizer

class NewsTokenizer(Tokenizer):
    """Tokenizer that skips newsgroups headers"""

    def __init__(self, split_re='\s+', filter_re='[^a-zA-Z]'):
        Tokenizer.__init__(self, split_re, filter_re)

    def tokenize(self, filename, buff):
        line = buff.readline()
        while line.strip() != '':
            line = buff.readline()
        return Tokenizer.tokenize(self, filename, buff)
