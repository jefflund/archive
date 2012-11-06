from pytopic.util.data import Reader

class XRefSet(object):

    def __init__(self, corpus):
        self.corpus = corpus
        self.refs = [set() for _ in self.corpus]

    def add_ref(self, from_title, to_title):
        from_index = self.corpus.titles.token_type(from_title)
        to_index = self.corpus.titles.token_type(to_title)
        self.refs[from_index].add(to_index)

    def __getitem__(self, doc_index):
        return self.refs[doc_index]


class XRefReader(Reader):

    def __init__(self, corpus):
        Reader.__init__(self)
        self.corpus = corpus

    def read(self):
        xrefs = XRefSet(self.corpus)
        for filename, buff in self.get_files():
            for line in buff:
                from_title, to_title = line.split()
                xrefs.add_ref(from_title, to_title)
        return xrefs
