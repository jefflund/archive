from scripts.dataset import (get_bible_corpus, get_bible_xrefs,
                             get_bible_concordance)

if __name__ == '__main__':
    corpus = get_bible_corpus()
    xrefs = get_bible_xrefs(corpus)
    concord = get_bible_concordance(corpus)
