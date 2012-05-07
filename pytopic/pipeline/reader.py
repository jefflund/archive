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


class BibleTokenizer(Tokenizer):
    """Tokenizer that treats each line as a title-document pair"""

    def __init__(self, split_re='\s+', filter_re='[^a-zA-Z]'):
        Tokenizer.__init__(self, split_re, filter_re)

    def tokenize(self, filename, buff):
        for line in buff:
            line = line.strip()
            if line == '':
                continue

            title, tokens = line.split(self.split_re, 1)
            tokens = [self.transform(token) for token in tokens]
            tokens = [token for token in tokens if self.keep(token)]
            if len(tokens) > 0:
                yield title, tokens

