"""Example of a run of lda"""

from __future__ import division
import pickle
from pipeline.corpus import CorpusReader
from pipeline.preprocess import filter_stopwords
from topic.vanilla import VanillaLDA
from util.data import load_stopwords
from topic.model import print_hook, combined_hook, time_hook


def main():
    T = 100
    alpha = 50 / T
    beta = .01

    stopwords_file = '/home/jlund3/workspace/Datasets/stopwords/english.txt'
    dataset_dir = '/home/jlund3/workspace/Datasets/20ng/groups'

    reader = CorpusReader()
    reader.add_dir(dataset_dir)
    corpus = reader.read()
    corpus = filter_stopwords(corpus, load_stopwords(stopwords_file))

    lda = VanillaLDA(corpus, T, alpha, beta)
    lda.output_hook = combined_hook(time_hook(lda), print_hook(lda, 10))
    lda.inference(500)
    lda.print_state(verbose=True)

if __name__ == '__main__':
    main()
