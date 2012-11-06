"""Functions for preprocessing a corpus"""

from pytopic.pipeline.corpus import Corpus

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

    transformed = Corpus()
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
