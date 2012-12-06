from scripts.dataset import get_newsgroups_corpus
from pytopic.topic.vanilla import VanillaLDA
from pytopic.util.handler import Printer, Timer

def get_model(corpus):
    lda = VanillaLDA(corpus, 20, .4, .01)
    lda.register_handler(Printer(10))
    lda.register_handler(Timer())
    return lda

if __name__ == '__main__':
    corpus = get_newsgroups_corpus()
    lda = get_model(corpus)
    lda.inference(100)
