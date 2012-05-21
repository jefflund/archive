from __future__ import division
from pytopic.topic.vanilla import VanillaLDA
from scripts.get_data import get_20ng
import os
from pytopic.topic.model import print_hook
from pytopic.classify.naive import validate_model



if __name__ == '__main__':
    params = [1, 4, 5, 6, 7, 8, 9]

    pssh_node = int(os.environ.get('PSSH_NODENUM', '2'))
    param_set = pssh_node % len(params)

    temp = params[param_set]
    print temp

    corpus = get_20ng()
    T = 100
    alpha = .05
    beta = .001

    lda = VanillaLDA(corpus, T, alpha, beta)
    lda.output_hook = print_hook(lda, 100)

    lda.set_anneal_temp(1 / temp)
    lda.inference(1000)

    print 'Accuracy: {}'.format(repr(validate_model(lda)))
