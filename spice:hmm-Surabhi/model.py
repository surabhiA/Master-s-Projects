#!/usr/bin/python
import numpy as np
from hmmlearn import hmm
from sklearn.metrics import accuracy_score
from collections import Counter
import sys, os

TRAIN = "data/ALLCAUSE_COST_SPICE.txt"
#data/ALLCAUSE_LOS_SPICE.txt
TEST = "data/private/ALLCAUSE_COST_SPICE_TEST.txt"
#data/private/ALLCAUSE_LOS_SPICE_TEST.txt
NUM_HIDDEN_STATES = 4
NUM_ITER = 30

#This function trains the Hidden markov model - Gaussian HMM
def train_model(iter):
	#creates an empty 2D array
	#X = np.array([], dtype=np.int16)
	#X = np.reshape(X, (-1, 1))
	X = []

	#Stores the length of the sequences-Given as parameter to train the model
	lengths = []

	#Stores the last element of the sequences-Used in calculation of baseline
	actual_baseline_labels = []

	fr = open(TRAIN ,"r")
	data_info = fr.readline().rstrip().split(" ")
	num_samples = int(data_info[0])
	num_states = int(data_info[1])

	count = 0
	for line in fr:
		row = line.rstrip().split(" ")
		seq_len = np.array(row[0], dtype=np.int16)
		seq = row[1:]
		label = int(row[int(row[0])])
		actual_baseline_labels.append(label)

		#reshape the sequence into a 2D array
		#seq = np.array(seq, dtype=np.int16)
		#seq = np.reshape(seq, (-1, 1))
		X.extend(seq)

		#append the length of sequences into a list
		lengths.append(seq_len)
		count += 1
		
		#concatenate all the training example sequences
		#X = np.concatenate((X, seq))
		if count % 100000 == 0:
			print ".",
			#sys.stdout.flush()

	X = np.array(X, dtype=np.int16)
	X = np.reshape(X, (-1, 1))
	fr.close()

	#trains the Gaussian HMM model
	#print "Training model with hidden_states: ", num_iter
	#sys.stdout.flush()
	model = hmm.GaussianHMM(n_components=NUM_HIDDEN_STATES,n_iter=NUM_ITER).fit(X, lengths)

	#print "Baseline labels",actual_baseline_labels
	return model,actual_baseline_labels


#Calculate baseline accuracy
def cal_baseline(actual_baseline_labels,num_samples,actual_values):
	#calculate majority label
	maj_label,num_most_common = Counter(actual_baseline_labels).most_common(1)[0]
	print "maj baseline label",maj_label

	predicted_values_baseline = [maj_label] * num_samples
	#print "Baseline pred values",predicted_values_baseline
	print "Baseline Accuracy"
	print accuracy_score(actual_values,predicted_values_baseline)


#Testing the model
def predict_test_data(model):
	fr = open(TEST ,"r")
	data_info = fr.readline().rstrip().split(" ")
	num_samples = int(data_info[0])
	num_states = data_info[1]

	actual_values = []
	predicted_values = []
	probabilities = []

	#print "Predicting values for model: "
	for line in fr:
		row = line.rstrip().split(" ")
		seq_len = int(row[0])
		seq = row[1:seq_len]
		label_value = row[seq_len]
		actual_values.append(int(label_value))

		#Converting the sequence into 2D array
		seq = np.array(seq, dtype=np.int16)
		seq = np.reshape(seq, (-1, 1))

		#Get probability of sequence with last element 0
		# X = np.concatenate((seq,[[0]]))
		# prob = model.decode(X,algorithm='viterbi')[0]
		# probabilities.append(prob)

		##Get probability of sequence with last element 1
		X = np.concatenate((seq,[[1]]))
		prob = model.decode(X,algorithm='viterbi')[0]
		probabilities.append(prob)

		##Get probability of sequence with last element 2
		X = np.concatenate((seq,[[2]]))
		prob = model.decode(X,algorithm='viterbi')[0]
		probabilities.append(prob)

		##Get probability of sequence with last element 3
		X = np.concatenate((seq,[[3]]))
		prob = model.decode(X,algorithm='viterbi')[0]
		probabilities.append(prob)

		##Get probability of sequence with last element 4
		X = np.concatenate((seq,[[4]]))
		prob = model.decode(X,algorithm='viterbi')[0]
		probabilities.append(prob)

		##Get probability of sequence with last element 5
		# X = np.concatenate((seq,[[5]]))
		# prob = model.decode(X,algorithm='viterbi')[0]
		# probabilities.append(prob)

		##Get probability of sequence with last element 6
		# X = np.concatenate((seq,[[6]]))
		# prob = model.decode(X,algorithm='viterbi')[0]
		# probabilities.append(prob)

		#get value which gives max probability if it is at the end of the prefix, and predict that value
		max_value = max(probabilities)
		pred_value = probabilities.index(max_value) + 1
		predicted_values.append(pred_value)
		probabilities = []

	fr.close()
	#print "model actual values",actual_values
	print "model pred values",predicted_values

	print "Model Accuracy"
	print accuracy_score(actual_values, predicted_values)

	return actual_values,num_samples


def main(iter):
	model,actual_baseline_labels = train_model(iter)
	actual_values,num_samples = predict_test_data(model)
	cal_baseline(actual_baseline_labels,num_samples,actual_values)

if __name__ == '__main__':
	sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)
	main(0)
	# print "------------------- 1 ------------------"
	# main(1)
	# print "------------------- 5 ------------------"
	# main(5)
	# print "------------------- 10 ------------------"
	# main(10)
	# print "------------------- 30 ------------------"
	# main(30)