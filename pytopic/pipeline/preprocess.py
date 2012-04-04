"""Functions for preprocessing a corpus"""

from pipeline.corpus import Corpus

def rare_words(corpus, threshold):
    """
    rare_words(Corpus, int): return Corpus
    Removes words from the Corpus that appear in fewer than a threshold amount
    of documents.
    """

    counts = [0 for _ in range(len(corpus.vocab))]
    for d in range(len(corpus)):
        for v in corpus[d]:
            counts[v] += 1
    stopwords = {v for v, count in enumerate(counts) if count < threshold}
    return _filter_types(corpus, stopwords)


def stopwords(corpus, *stopword_filenames):
    """
    stopwords(Corpus, *str): return Corpus
    Returns a new Corpus with all stopwords in the given stopwords files
    removed from the data. The stopword files must contain one stopword per
    line.
    """

    stopwords = set()
    for filename in stopword_filenames:
        for word in open(filename):
            word = word.strip()
            if len(word) > 0:
                stopwords.add(word)
    stopwords = {corpus.vocab.token_type(v) for v in stopwords}
    return _filter_types(corpus, stopwords)


def _filter_types(corpus, stopwords):
    transformed = Corpus()
    for d in range(len(corpus)):
        tokens = [v for v in corpus[d] if v not in stopwords]
        if len(tokens) > 0:
            transformed.add_document(corpus.titles[d],
                                     [corpus.vocab[v] for v in tokens])
    return transformed

