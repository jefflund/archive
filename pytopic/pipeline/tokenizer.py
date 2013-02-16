"""Special corpora-specific corpus building tools"""

import re
import StringIO
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

            split = self.split_re.split(line)
            title, tokens = split[0], split[1:]
            tokens = [self.transform(token) for token in tokens]
            tokens = [token for token in tokens if self.keep(token)]
            if len(tokens) > 0:
                yield title, tokens


class HTMLTokenizer(Tokenizer):
    """Tokenizer that extracts text from html files using nltk"""

    def __init__(self, split_re='\s+', filter_re='[^a-zA-Z]'):
        Tokenizer.__init__(self, split_re, filter_re)

    def tokenize(self, filename, buff):
        # borrowed from nltk.clean_html, which falls under Apache License 2.0
        text = buff.read().strip()
        text = re.sub(r'(?is)<(script|style).*?>.*?(</\1>)', '', text)
        text = re.sub(r'(?s)<!--(.*?)-->[\n]?', '', text)
        text = re.sub(r'(?s)<.*?>', ' ', text)
        text = re.sub(r'&nbsp;', ' ', text)
        text = re.sub(r'  ', ' ', text)
        text = re.sub(r'  ', ' ', text)
        text = text.strip()
        return Tokenizer.tokenize(self, filename, StringIO.StringIO(text))
