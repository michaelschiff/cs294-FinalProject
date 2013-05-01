import sys
from itertools import izip_longest
from sklearn import tree
from scipy.sparse import dok_matrix
from numpy import array, append

# for iterating over a list in chunks
def grouper(iterable, n, fillvalue=None):
    args = [iter(iterable)] * n
    return izip_longest(*args, fillvalue=fillvalue)

class DTree:
    def train(self, datafile, num_features):
        examples = 0
        with open(datafile, "r") as data:
            for line in data:
                examples += 1
        X = dok_matrix((examples, num_features))
        Y = []
        examples = 0
        with open(datafile, "r") as data:
            for line in data:
                splitLine = line.split()
                Y.append(splitLine[0])
                splitLine = splitLine[1:]
                for (i,v) in grouper(splitLine, 2):
                    X[examples, int(i)] = float(v)
                examples += 1
        clf = tree.DecisionTreeClassifier()
        clf = clf.fit(X.todense(), Y)

if __name__ == "__main__":
    dt = DTree()
    dt.train(sys.argv[1], int(sys.argv[2]))
