"""Example of a run of lda"""

from __future__ import division

from pipeline.corpus import CorpusReader, Tokenizer
from util.data import load_stopwords
from topic.vanilla import VanillaLDA

def main():
    T = 100
    alpha = 50 / T
    beta = .01

    stopwords_file = '/home/jlund3/workspace/Datasets/stopwords/english.txt'
    dataset_dir = '/home/jlund3/workspace/Datasets/20ng/groups'

    stopwords = load_stopwords(stopwords_file)
    tokenizer = Tokenizer(stopwords)
    reader = CorpusReader(tokenizer)
    reader.add_dir(dataset_dir)
    corpus = reader.read()

    lda = VanillaLDA(corpus, T, alpha, beta)
    lda.inference(500)
    lda.print_state(verbose=True)

if __name__ == '__main__':
    main()
