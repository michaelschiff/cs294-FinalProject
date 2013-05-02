import numpy as np
import cPickle
from sklearn.naive_bayes import GaussianNB
from sklearn.naive_bayes import MultinomialNB
from sklearn.preprocessing import StandardScaler
from sklearn.cross_validation import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.neighbors import KNeighborsClassifier
from scipy.sparse import csr_matrix
from sklearn import metrics
import sys

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

def main(data_file, num_features, num_samples):
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
					ij[1].append(int(value)-1)
					if int(value) > num_features:
						print int(value)
				else:
					data.append(int(value))

	print len(data), len(ij[0])
	data_set = csr_matrix((data, ij), shape=(num_samples, num_features))
	data_set = StandardScaler(with_mean=False).fit_transform(data_set.astype(np.double))
	classifier = GaussianNB()

	X_train, X_test, y_train, y_test = train_test_split(data_set.toarray(), labels, test_size=0.8)

	classifier.fit(X_train, y_train)
	pred_labels = classifier.predict(X_test)
	precision, recall, f1_score, support = metrics.precision_recall_fscore_support(y_test, pred_labels)
	print 'Gaussian NB: '
	print 'precision:', precision
	print 'recall:', recall
	print 'f1:', f1_score
	print '# occurrences:', support
	fpr, tpr, thresholds = metrics.roc_curve(y_test, pred_labels, 1)
	print 'fpr:', fpr
	print 'tpr:', tpr
	print 'thresholds:', thresholds
	auc_score = metrics.auc_score(y_test, pred_labels)
	print 'auc:', auc_score
	with open('naive_bayes.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)
	print ''


	classifier = MultinomialNB(alpha=0.5)
	classifier.fit(X_train, y_train)
	pred_labels = classifier.predict(X_test)
	precision, recall, f1_score, support = metrics.precision_recall_fscore_support(y_test, pred_labels)
	print 'Multinomial NB: '
	print 'precision:', precision
	print 'recall:', recall
	print 'f1:', f1_score
	print '# occurrences:', support
	fpr, tpr, thresholds = metrics.roc_curve(y_test, pred_labels, 1)
	print 'fpr:', fpr
	print 'tpr:', tpr
	print 'thresholds:', thresholds
	auc_score = metrics.auc_score(y_test, pred_labels)
	print 'auc:', auc_score
	with open('multinomialNB-alpha-0.5.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)
	print ''

	classifier = LogisticRegression()
	classifier.fit(X_train, y_train)
	pred_labels = classifier.predict(X_test)
	precision, recall, f1_score, support = metrics.precision_recall_fscore_support(y_test, pred_labels)
	print 'Logistic Regression: '
	print 'precision:', precision
	print 'recall:', recall
	print 'f1:', f1_score
	print '# occurrences:', support
	fpr, tpr, thresholds = metrics.roc_curve(y_test, pred_labels, 1)
	print 'fpr:', fpr
	print 'tpr:', tpr
	print 'thresholds:', thresholds
	auc_score = metrics.auc_score(y_test, pred_labels)
	print 'auc:', auc_score
	with open('logisticregression.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)
	print ''

	classifier = KNeighborsClassifier()
	classifier.fit(X_train, y_train)
	pred_labels = classifier.predict(X_test)
	precision, recall, f1_score, support = metrics.precision_recall_fscore_support(y_test, pred_labels)
	print 'K-Neighbors (5): '
	print 'precision:', precision
	print 'recall:', recall
	print 'f1:', f1_score
	print '# occurrences:', support
	fpr, tpr, thresholds = metrics.roc_curve(y_test, pred_labels, 1)
	print 'fpr:', fpr
	print 'tpr:', tpr
	print 'thresholds:', thresholds
	auc_score = metrics.auc_score(y_test, pred_labels)
	print 'auc:', auc_score
	with open('kneighbors-5.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)
	print ''
	
	

	
	# tp = 0
	# tn = 0
	# fp = 0
	# fn = 0
	# for index, test_vector in enumerate(data[int(len(data)*0.90):]):
	# 	prediction = classifier.pred(test_vector)
	# 	print prediction,
	# 	if prediction and prediction == labels[int(len(labels)*0.90) + index]:
	# 		tp += 1
	# 	elif prediction and prediction != labels[int(len(labels)*0.90) + index]:
	# 		fn += 1
	# 	elif not prediction and prediction == labels[int(len(labels)*0.90) + index]:
	# 		tn += 1
	# 	else:
	# 		fp += 1

	# print 'tp:', tp, 'tn', tn, 'fp', fp, 'fn', fn

if __name__ == '__main__':
	main(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
