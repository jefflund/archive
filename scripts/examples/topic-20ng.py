from pytopic.model import vanilla
from pytopic.util import handler
from scripts.corpora import newsgroups

def get_model(corpus):
    lda = vanilla.VanillaLDA(corpus, 20, .4, .01)
    lda.register_handler(handler.Timer())
    lda.register_handler(handler.Printer(10))
    return lda


if __name__ == '__main__':
    corpus = newsgroups.get_corpus()
    lda = get_model(corpus)
    lda.register_handler(handler.TopicConvergenceCheck(lda.z))
    lda.set_inference('ccm')
    lda.inference(100)
