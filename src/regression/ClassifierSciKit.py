import numpy as np
import cPickle
from sklearn.naive_bayes import GaussianNB
from sklearn.naive_bayes import MultinomialNB
from sklearn.preprocessing import StandardScaler
from sklearn.cross_validation import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.neighbors import KNeighborsClassifier
from scipy.sparse import csr_matrix
import sys

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
	score = classifier.score(X_test, y_test)
	print 'Gaussian NB: ', score
	with open('naive_bayes.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)


	classifier = MultinomialNB(alpha=0.5)
	classifier.fit(X_train, y_train)
	score = classifier.score(X_test, y_test)
	print 'Multinomial NB:', score
	with open('multinomialNB-alpha-0.5.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)

	classifier = LogisticRegression()
	classifier.fit(X_train, y_train)
	score = classifier.score(X_test, y_test)
	print 'Logistic Regression:', score
	with open('logisticregression.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)

	classifier = KNeighborsClassifier()
	classifier.fit(X_train, y_train)
	score = classifier.score(X_test, y_test)
	print 'KNeighbors (5):', score
	with open('kneighbors-5.pkl', 'wb') as fid:
		cPickle.dump(classifier, fid)
	
	

	
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
