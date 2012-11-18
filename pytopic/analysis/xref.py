from __future__ import division

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

    def __iter__(self):
        return iter(self.refs)

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


class Concordance(object):
    """Provides an invert-index of words to documents"""

    def __init__(self, corpus):
        self.corpus = corpus
        self.index = [set() for _ in range(len(self.corpus.vocab))]
        for doc_id, doc in enumerate(self.corpus):
            for word in set(doc):
                self.index[word].add(doc_id)

    def lookup(self, *words):
        """
        Concordance.lookup(*int): return set of int
        Returns the document ids which contain all of the given token ids
        """

        doc_sets = [self.index[word] for word in words]
        return set.intersection(*doc_sets)


def precision_recall(xrefs, model):
    """
    precision_recall(XRefSet, xrefmodel): return float, float
    Calculates the precision and recall of a model with cross references with
    respect to a base XRefSet
    """

    tp, fp, fn = 0, 0, 0
    for gold, pred in zip(xrefs, model):
        tp += len(gold & pred)
        fp += len(pred - gold)
        fn += len(gold - pred)
    precision = tp / (tp + fp)
    recall = tp / (tp + fn)
    return precision, recall
