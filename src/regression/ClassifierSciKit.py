import numpy as np
import cPickle
from sklearn.naive_bayes import GaussianNB
from sklearn.naive_bayes import MultinomialNB
from sklearn.preprocessing import StandardScaler
from sklearn.cross_validation import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.neighbors import KNeighborsClassifier
from scipy.sparse import csr_matrix, dok_matrix
from sklearn import metrics
import sys
from itertools import izip_longest

# python ClassifierSciKit.py ../../FeaturizationNoMRDC/v1Matrix.txt 44442 100
class Classifier:
    def __init__(self, classifier, name):
        self.name = name
        self.classifier = classifier
    def train(self, x, y):
        self.classifier = self.classifier.fit(x, y)
    def predict(self, x):
        ad = self.classifier.predict(x)
        return self.classifier.predict(x)
    def evaluate(self, x, y):
        pred = self.predict(x)
        
        pred_prob = self.roc_plot_input(x)
        print len(pred), len(pred_prob), len(y)
        precision, recall, f1_score, support = metrics.precision_recall_fscore_support(y, pred)
        fpr, tpr, thresholds = metrics.roc_curve(y, pred_prob)
        auc_score = metrics.auc_score(y.astype(np.double), pred_prob)
        self.writeYPred(y,pred_prob)
        return (precision, recall, f1_score, support, fpr, tpr, thresholds, auc_score)
	
    def writeYPred(self,y,pred):
        f = open('YPred.txt','wb')
        for y_i,p in zip(y,pred):
            f.write(str(y_i)+' '+str(p)+'\n')
        f.close()
        return

    def roc_plot_input(self, x):
        return [c[1] for c in self.classifier.predict_proba(x)]

class ClassifierPool:
    def __init__(self, classifiers):
        self.classifiers = classifiers
    def train(self, x, y):
        for classifier in self.classifiers:
            classifier.train(x,y)
    def predict(self, x):
        votes = np.array([0]*len(x))
        for classifier in self.classifiers:
            votes = np.add(votes, classifier.predict(x))
        predictions = np.array([0]*len(x))
        for voteIdx in xrange(len(votes)):
            if votes[voteIdx] > len(self.classifiers)/2.0: predictions[voteIdx] = 1
        return predictions
    def evaluate(self, x, y):
        pred = self.predict(x)
        precision, recall, f1_score, support = metrics.precision_recall_fscore_support(y, pred)
        fpr, tpr, thresholds = metrics.roc_curve(y, pred)
        auc_score = metrics.auc_score(y, pred)
        self.print_metrics((precision, recall, f1_score, support, fpr, tpr, thresholds, auc_score), "Ensemble Classifier")
    
    def individualEvaluate(self, x, y):
        for classifier in self.classifiers:
            self.print_metrics(classifier.evaluate(x, y), classifier.name)

    def print_metrics(self, (precision, recall, f1_score, support, fpr, tpr, thresholds, auc_score), name):
        print name
        print 'precision:', precision
        print 'recall:', recall
        print 'f1:', f1_score
        print '# occurrences:', support
        print 'fpr:', fpr
        print 'tpr:', tpr
        print 'thresholds:', thresholds
        print 'auc:', auc_score
        print "\n"

def load_data(datafile, num_features):
    def grouper(iterable, n, fillvalue=None):
        args = [iter(iterable)] * n
        return izip_longest(*args, fillvalue=fillvalue)  
    
    holdoutOffset = 0
    trainingExamples = 0
    holdoutExamples = 0
    print "counting Examples"
    with open(datafile, "r") as data:
        for line in data:
            if (trainingExamples + holdoutExamples + holdoutOffset)%10 == 0:
                holdoutExamples += 1
            else:
                trainingExamples += 1
    print str(trainingExamples) + " training examples"
    print str(holdoutExamples) + " holdout examples"
    print "building matrices"
    tX = dok_matrix((trainingExamples, num_features))
    tY = []
    hX = dok_matrix((holdoutExamples, num_features))
    hY = []
    trainingExamples = 0
    holdoutExamples = 0
    with open(datafile, "r") as data:
        for line in data:
            splitLine = line.split()
            label = int(splitLine[0])
            splitLine = splitLine[1:]
            if (trainingExamples + holdoutExamples + holdoutOffset)%10 == 0:
                hY.append(label)
                for (i,v) in grouper(splitLine, 2):
                    hX[holdoutExamples, int(i)-1] = float(v)
                holdoutExamples += 1
            else:
                tY.append(label)
                for (i,v) in grouper(splitLine, 2):
                    tX[trainingExamples, int(i)-1] = float(v)
                trainingExamples += 1
    tX = StandardScaler(with_mean=False).fit_transform(tX.todense())
    hX = StandardScaler(with_mean=False).fit_transform(hX.todense())
    
    return (tX, hX, np.array(tY), np.array(hY))

def load_data1(data_file, num_features, num_samples):
    sillyNumber = 1
    labels = []
    data = []
    ij = [[],[]]
    with open(data_file, 'r') as f:
        
        for i, line in enumerate(f):
            if i >= num_samples:
                break
            components = line.split(' ')
            labels.append(int(components[0]))
            
            for index, value in enumerate(components[1:]):
                if index % 2 == 0:
                    ij[0].append(i)
                    ij[1].append(int(value)-sillyNumber)
                    if int(value) > num_features:
                        print int(value)
                else:
                    data.append(float(value))

    print len(data), len(ij[0])
    data_set = csr_matrix((data, ij), shape=(num_samples, num_features))
    data_set = StandardScaler(with_mean=False).fit_transform(data_set.astype(np.double))
    return train_test_split(data_set.toarray(), labels, test_size=0.8)


def build_feature_dict(token_dict_file, tag_dict_file, pos_dict_file):
    feature_dict = {}
    num_features = 0
    with open(token_dict_file, 'r') as f:
        for line in f:
            feature_dict[num_features] = 'token_' + line.split('\t')[0]
            num_features += 1
    with open(tag_dict_file, 'r') as f:
        for line in f:
            feature_dict[num_features] = 'tag_' + line.split('\t')[0]
            num_features += 1
    with open(pos_dict_file, 'r') as f:
        for line in f:
            feature_dict[num_features] = 'pos_' + line.split('\t')[0]
            num_features += 1
    feature_dict[num_features] = 'numEdits'
    num_features += 1 
    feature_dict[num_features] = 'editTimeElapsed'
    num_features += 1 
    feature_dict[num_features] = 'numCodeExamples'
    num_features += 1 
    feature_dict[num_features] = 'numQuestionBodyWords'
    num_features += 1 
    feature_dict[num_features] = 'numSentencesForQuestionBody'
    num_features += 1 
    feature_dict[num_features] = 'numCodeBodyWords'
    num_features += 1 
    with open('feature_dict.txt', 'wb') as f:
        cPickle.dump(feature_dict, f)
    return feature_dict

if __name__ == "__main__":
    X_train, X_test, y_train, y_test = load_data1(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
    # pool = ClassifierPool([Classifier(GaussianNB(), "GNB"), Classifier(MultinomialNB(alpha=0.5), "MNB"), Classifier(LogisticRegression(), "LR"), Classifier(KNeighborsClassifier(), "KNN")])
    pool = ClassifierPool([Classifier(LogisticRegression(), "LR")])
    pool.train(X_train, y_train)
    # pool.evaluate(X_test, y_test)
    pool.individualEvaluate(X_test, y_test)
