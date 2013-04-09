"""Functions for preprocessing a corpus"""

from pytopic.pipeline import dataset
from pytopic.util import compute

def filter_rarewords(corpus, threshold, retain_empty=False):
    """
    rare_words(Corpus, int): return Corpus
    Removes words from the Corpus that appear in fewer than a threshold amount
    of documents.
    """

    counts = [0 for _ in range(len(corpus.vocab))]
    for d in range(len(corpus)):
        for v in corpus[d]:
            counts[v] += 1
    rares = {corpus.vocab[v] for v, n in enumerate(counts) if n < threshold}
    return filter_stopwords(corpus, rares, retain_empty)


def filter_stopwords(corpus, stopwords, retain_empty=False):
    """
    filter_stopwords(Corpus, set of str): return Corpus
    Returns a new Corpus with all the stopwords in the given set removed from
    the data.
    """

    transformed = dataset.Corpus()
    for d in range(len(corpus)):
        tokens = [corpus.vocab[v] for v in corpus[d]]
        tokens = [v for v in tokens if v not in stopwords]
        if len(tokens) > 0 or retain_empty:
            transformed.add_document(corpus.titles[d], tokens)
    return transformed

def load_stopwords(*stopword_filenames):
    """
    load_stopwords(*str): return set of str
    Reads stopword files with one stopword per line into a set
    """

    stopwords = set()
    for filename in stopword_filenames:
        for word in open(filename):
            word = word.strip()
            if len(word) > 0:
                stopwords.add(word)
    return stopwords

def split_corpus(corpus, training_proportion):
    """
    split_corpus(Corpus, float): return Corpus, Corpus
    Splits a corpus into a training and test set. Both corpora will have the
    complete vocabularly of the original Corpus, and the documents are shuffled
    before splitting, to ensure an even mix of document types.
    """

    training = dataset.Corpus()
    test = dataset.Corpus()
    training.vocab = corpus.vocab
    test.vocab = corpus.vocab

    break_point = int(len(corpus) * training_proportion)
    doc_ids = compute.sample_order(len(corpus))

    for d in doc_ids[:break_point]:
        doc_index = training.titles.add_unique(corpus.titles[d])
        training.data[doc_index] = corpus.data[d]
    for d in doc_ids[break_point:]:
        doc_index = test.titles.add_unique(corpus.titles[d])
        test.data[doc_index] = corpus.data[d]

    return training, test
