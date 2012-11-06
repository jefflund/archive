class XRefSet(object):

    def __init__(self, corpus):
        self.corpus = corpus
        self.refs = [set() for _ in self.corpus)]

    def add_ref(self, from_title, to_title):
        from_index = self.corpus.titles[from_title]
        to_index = self.corpus.titles[to_title]
        self.refs[from_index].add(to_index)

    def __getitem__(self, doc_index):
        return self.refs[doc_index]
