import numpy as numpy
import mlpy
import sys

def main(data_file, num_features):
	labels = []
	data = []
	with open(data_file, 'r') as f:
		for i, line in enumerate(f):
			components = line.split(' ')
			labels.append(components[0])
			features = [0] * num_features
			for index, value in enumerate(components[1:]):
				if index % 2 == 0:
					coord = int(value)-1
				else:
					features[coord] = int(value)
			data.append(features)

			if i > 2000:
				break

	classifier = mlpy.LibLinear()
	classifier.learn(data[:int(len(data)*0.9)], labels[:int(len(data)*0.9)])
	tp = 0
	tn = 0
	fp = 0
	fn = 0
	for index, test_vector in enumerate(data[int(len(data)*0.90):]):
		prediction = classifier.pred(test_vector)
		print prediction,
		if prediction and prediction == labels[int(len(labels)*0.90) + index]:
			tp += 1
		elif prediction and prediction != labels[int(len(labels)*0.90) + index]:
			fn += 1
		elif not prediction and prediction == labels[int(len(labels)*0.90) + index]:
			tn += 1
		else:
			fp += 1

	print 'tp:', tp, 'tn', tn, 'fp', fp, 'fn', fn

if __name__ == '__main__':
	main(sys.argv[1], int(sys.argv[2]))
