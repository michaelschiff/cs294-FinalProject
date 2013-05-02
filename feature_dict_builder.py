import sys
import cPickle

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

def main(token_dict_file, tag_dict_file, pos_dict_file):
	build_feature_dict(token_dict_file, tag_dict_file, pos_dict_file)


if __name__ == '__main__':
	main(sys.argv[1], sys.argv[2], sys.argv[3])
