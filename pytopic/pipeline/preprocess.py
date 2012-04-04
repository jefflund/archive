"""Functions for preprocessing a corpus"""

from pipeline.corpus import Corpus

def rare_words(corpus, threshold):
    """
    rare_words(Corpus, int): return Corpus
    Removes words from the Corpus that appear in fewer than a threshold amount
    of documents.
    """

    counts = [0 for _ in len(corpus.vocab)]
    for doc in corpus:
        for v in doc:
            counts[v] += 1
    stopwords = {v for v, count in enumerate(counts) if count < threshold}

    transformed = Corpus()
    for d in range(len(corpus)):
        tokens = [v for v in doc if v not in stopwords]
        if len(tokens) > 0:
            transformed.add_document(corpus.titles[d],
                                     [corpus.vocab[v] for v in tokens])
    return transformed


def stopwords(corpus, *stopword_filenames):
    stopwords = set()
    for filename in stopword_filenames:
        for word in open(filename):
            word = word.strip()
            if len(word) > 0:
                stopwords.add(word)


def tf_idf(corpus, threshold):
    return corpus
