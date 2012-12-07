from scripts.dataset.bible import get_corpus
from pytopic.topic.vanilla import VanillaLDA
from pytopic.util.handler import Timer, Printer

def get_lda(corpus):
    lda = VanillaLDA(corpus, 1000, .1, .01)
    lda.register_handler(Timer())
    lda.register_handler(Printer(10))
    return lda

if __name__ == '__main__':
    corpus = get_corpus()
    lda = get_lda(corpus)
    lda.inference(100)
