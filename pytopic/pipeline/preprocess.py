"""Functions for preprocessing a corpus"""

from pytopic.pipeline.corpus import Corpus

def filter_rarewords(corpus, threshold):
    """
    rare_words(Corpus, int): return Corpus
    Removes words from the Corpus that appear in fewer than a threshold amount
    of documents.
    """

    counts = [0 for _ in range(len(corpus.vocab))]
    for d in range(len(corpus)):
        for v in corpus[d]:
            counts[v] += 1
    rarewords = {corpus.vocab[v] for v, count in enumerate(counts)
                  if count < threshold}
    return filter_stopwords(corpus, rarewords)


def filter_stopwords(corpus, stopwords):
    """
    stopwords(Corpus, *str): return Corpus
    Returns a new Corpus with all stopwords in the given stopwords files
    removed from the data. The stopword files must contain one stopword per
    line.
    """

    transformed = Corpus()
    for d in range(len(corpus)):
        tokens = [corpus.vocab[v] for v in corpus[d]]
        tokens = [v for v in tokens if v not in stopwords]
        if len(tokens) > 0:
            transformed.add_document(corpus.titles[d], tokens)
    return transformed
