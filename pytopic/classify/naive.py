from __future__ import division

class NaiveBayes(object):

    def __init__(self, labels, data):
        label_set = set(labels)
        word_set = set()
        for doc in data:
            word_set.update(doc)

        self.label_counts = {l: 0 for l in label_set}
        self.word_counts = {l: {w: 0 for w in word_set} for l in label_set}

        for label, doc in data:
            self.label_counts[label] += 1
            for word in doc:
                self.word_counts[label][word] += 1

        label_norm = len(data)
        for label in label_set:
            self.label_counts[label] /= label_norm
            word_norm = len(self.word_counts[label])
            for word in word_set:
                self.word_counts[label][word] /= word_norm
