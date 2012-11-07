from pytopic.util.data import Reader

class XRefSet(object):
    """A collection of cross references within a Corpus"""

    def __init__(self, corpus):
        self.corpus = corpus
        self.refs = [set() for _ in self.corpus]

    def add_ref(self, from_title, to_title):
        """
        XRefSet.add_ref(str, str): return None
        Adds a reference from one document to another, indexed by title.
        """

        from_index = self.corpus.titles.token_type(from_title)
        to_index = self.corpus.titles.token_type(to_title)
        self.refs[from_index].add(to_index)

    def __getitem__(self, doc_index):
        return self.refs[doc_index]

    def __len__(self):
        return len(self.refs)


class XRefReader(Reader):
    """Constructs an XRefSet from a set of files"""

    def __init__(self, corpus):
        Reader.__init__(self)
        self.corpus = corpus

    def read(self):
        """
        XRefReader.read(): return XRefSet
        Reads all the files and constructs an XRefSet
        """

        xrefs = XRefSet(self.corpus)
        for filename, buff in self.get_files():
            for line in buff:
                from_title, to_title = line.split()
                xrefs.add_ref(from_title, to_title)
        return xrefs
