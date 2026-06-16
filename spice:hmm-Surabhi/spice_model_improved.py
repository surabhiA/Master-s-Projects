#!/usr/bin/python
#Automated version of model.py-removed hardcoding

import numpy as np
from hmmlearn import hmm
from sklearn.metrics import accuracy_score
from collections import Counter
import sys, os
import operator

# State the problem number
problem_number = '0'

# and the user id (given during registration)
user_id = '48'

# name of this submission (only letters, numbers and _)
name = "0_HMM"

TRAIN = 'spice_data/Train/0.spice.train.80.txt'
#spice_data/train/0.spice.train.80.txt
#train_spice.txt
TEST = 'spice_data/Test/0.spice.test.txt'
#spice_data/test/0.spice.test.txt
#test_spice.txt

NUM_HIDDEN_STATES = 6
NUM_ITER = 30
num_states = 0

np.seterr(divide='ignore', invalid='ignore')
#This function trains the Hidden markov model - Gaussian HMM
def train_model():
    global NUM_HIDDEN_STATES,num_states

    #creates an list
    X = []

    #Stores the length of the sequences-Given as parameter to train the model
    lengths = []

    #Stores the last element of the sequences-Used in calculation of baseline
    actual_baseline_labels = []

    fr = open(TRAIN ,"r")
    data_info = fr.readline().rstrip().split(" ")
    num_samples = int(data_info[0])
    num_states = int(data_info[1]) + 1
    #NUM_HIDDEN_STATES = num_states

    count = 0
    for line in fr:
        row = line.rstrip().split(" ")
        seq_len = np.array(int(row[0]) + 1, dtype=np.int16)
        seq = row[1:]
        if(len(seq) < 3):
            continue

        #append -1 to the end of sequence and increase number of classes by 1
        seq.append("-1")

        label = int(seq[int(row[0])])  #after adding -1
        actual_baseline_labels.append(label)

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
    model = hmm.GaussianHMM(n_components=NUM_HIDDEN_STATES,n_iter=NUM_ITER).fit(X, lengths)
    print model.startprob_
    print model.transmat_

    #print "Baseline labels",actual_baseline_labels
    return model,actual_baseline_labels


#Testing the model
def predict_test_data(model):
    fr = open(TEST ,"r")
    data_info = fr.readline().rstrip().split(" ")
    num_samples = int(data_info[0])

    actual_values = []
    predicted_values = []
    probabilities = {}
    ranking = []

    for line in fr:
        row = line.rstrip().split(" ")
        seq_len = int(row[0])
        # if(seq_len == 0):
        #     continue
        seq = row[1:seq_len]
        label_value = row[seq_len]
        actual_values.append(int(label_value))

        #Converting the sequence into 2D array
        seq = np.array(seq, dtype=np.int16)
        seq = np.reshape(seq, (-1, 1))

        for index in range(-1,(num_states - 1)):
            #Get probability of sequence with last element 0
            X = np.concatenate((seq,[[index]]))
            prob = model.decode(X,algorithm='viterbi')[0]
            probabilities[index] = prob
            X.fill(0)

        #print "Prob: ", probabilities

        #Get probability of sequence with last element 0
        # X = np.concatenate((seq,[[0]]))
        # prob = model.decode(X,algorithm='viterbi')[0]
        # probabilities.append(prob)



        #get value which gives max probability if it is at the end of the prefix, and predict that value
        sorted_prob = sorted(probabilities.items(), key=operator.itemgetter(1))
        #print "Sorted prob: ", sorted_prob

        count = 0
        #iterate on reversed list
        for index in sorted_prob[::-1]:
            count += 1
            ranking.append(index[0])
            if(count == 5):
                break

        #print "Ranking:", ranking
        predicted_values.append(ranking[0])
        probabilities = {}
        ranking = []

    fr.close()
    print "model actual values",actual_values
    print "model pred values",predicted_values

    print "Model Accuracy"
    print accuracy_score(actual_values, predicted_values)

    return actual_values,num_samples


#Calculate baseline accuracy
def cal_baseline(actual_baseline_labels,num_samples,actual_values):
    #calculate majority label
    maj_label,num_most_common = Counter(actual_baseline_labels).most_common(1)[0]
    print "maj baseline label",maj_label

    predicted_values_baseline = [maj_label] * num_samples
    #print "Baseline pred values",predicted_values_baseline
    print "Baseline Accuracy"
    print accuracy_score(actual_values,predicted_values_baseline)


def main():
    # learn the model
    print ("Start Learning")
    model,actual_baseline_labels = train_model()
    print ("End of learning phase")

    print ("Start Predicting")
    actual_values,num_samples = predict_test_data(model)
    print ("End of predicting phase")

    cal_baseline(actual_baseline_labels,num_samples,actual_values)

####################################################### 

if __name__ == '__main__':
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)
    main()