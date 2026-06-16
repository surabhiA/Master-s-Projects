#!/usr/bin/python

import numpy as np
from hmmlearn.hmm import MultinomialHMM
from sklearn.metrics import accuracy_score
from collections import Counter
import sys, os
import operator
import utility_func

#Actual training file
TRAIN_FILE = 'data/four_or_more/age_less_65/male/ALLCAUSE_LOS_PUBLIC_SPICE.csv'

#Actual testing file
TEST_FILE = 'data/four_or_more/age_less_65/male/private/ALLCAUSE_LOS_PRIVATE_SPICE.csv'

#If file is los/cost where conversion needs to happen, the converted train file is stored as CONVERTED_TRAIN
CONVERTED_TRAIN = 'data/four_or_more/age_less_65/male/ConvertedFormat/ALLCAUSE_LOS_PUBLIC_SPICE.csv'

#If file is los/cost where conversion needs to happen, the converted test file is stored as CONVERTED_TEST
CONVERTED_TEST = 'data/four_or_more/age_less_65/male/ConvertedFormat/private/ALLCAUSE_LOS_PRIVATE_SPICE.csv'

NUM_HIDDEN_STATES = 2
NUM_ITER = 50

np.seterr(divide='ignore', invalid='ignore')

#If the command line argument is 1, that means conversion needs to happen. If 0, no conversion is needed.
#This function converts the original train and test files if arg is 1.
def dataPrep():
    if(sys.argv[1] == '1'):
        #utility_func is a separate file which contains all the supplementary functions.
        utility_func.convertFormat(TRAIN_FILE, CONVERTED_TRAIN)
        utility_func.convertFormat(TEST_FILE, CONVERTED_TEST)


#This function trains the Hidden markov model - Multinomial HMM
def train_model(TRAIN):

    #Stores the training set
    X = []

    #Stores the length of the sequences-Given as parameter to train the model
    lengths = []

    #Stores the last element of the sequences-Used in calculation of baseline
    actual_baseline_labels = []

    fr = open(TRAIN ,"r")
    #info obtained from header
    data_info = fr.readline().rstrip().split(" ")

    for line in fr:
        #Stores the entire line
        row = line.rstrip().split(" ")

        #Stores the length of sequence
        seq_len = int(row[0])

        #Stores the actual sequence
        seq = row[1:]

        #Stores the last element in the sequence
        label = int(seq[seq_len - 1])
        actual_baseline_labels.append(label)

        #Extend X- This is as per the format required by the model.
        X.extend(seq)

        #append the length of sequences into a list
        lengths.append(seq_len)

    #Convert X and lengths into format required by the model- Array
    X = np.array(X, dtype=np.int16)
    X = np.reshape(X, (-1, 1))
    lengths = np.array(lengths, dtype=np.int16)
    
    fr.close()

    #trains the Multinomial HMM model
    model = MultinomialHMM(n_components=NUM_HIDDEN_STATES,n_iter=NUM_ITER).fit(X, lengths)
    return model,actual_baseline_labels


#Testing the model
def predict_test_data(model, TEST):
    fr = open(TEST ,"r")
    data_info = fr.readline().rstrip().split(" ")
    #num_samples = int(data_info[0])
    num_symbols = int(data_info[1])
    num_samples = 0

    actual_values = []
    predicted_values = []
    probabilities = {}

    for line in fr:
        #Stores the entire line
        row = line.rstrip().split(" ")
        num_samples += 1

        #Stores the length of the sequence
        seq_len = int(row[0])

        #get the sequence except last element-We need to predict that
        seq = row[1:seq_len]

        #Get the last element of the row as the label
        label_value = int(row[seq_len])

        #append label to actual values.
        actual_values.append(label_value)

        #Converting the sequence into 2D array
        seq = np.array(seq, dtype=np.int16)
        seq = np.reshape(seq, (-1, 1))

        #Append each symbol at the end of the sequence and get the probability
        for index in range(num_symbols):
            X = np.concatenate((seq,[[index]]))
            prob = model.decode(X,algorithm='viterbi')[0]
            probabilities[index] = prob
            X.fill(0)


        #get value which gives max probability if it is at the end of the prefix, and predict that value
        sorted_prob = sorted(probabilities.items(), key=operator.itemgetter(1))
        
        #append symbol with highest probability to predicted_values
        predicted_values.append(sorted_prob[len(sorted_prob) - 1][0])
        probabilities = {}


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

    print "Baseline Accuracy"
    print accuracy_score(actual_values,predicted_values_baseline)


def main():
    global TRAIN_FILE, TEST_FILE

    #Command line argument 1/0 needed.
    dataPrep()

    #This is done to avoid code repetition and if statements.
    if(sys.argv[1] == '1'):
        #print "main"
        TRAIN_FILE = CONVERTED_TRAIN
        TEST_FILE = CONVERTED_TEST

    # learn the model
    print ("Start Learning")
    model,actual_baseline_labels = train_model(TRAIN_FILE)
    print ("End of learning phase")

    #Predict
    print ("Start Predicting")
    actual_values,num_samples = predict_test_data(model,TEST_FILE)
    print ("End of predicting phase")

    cal_baseline(actual_baseline_labels,num_samples,actual_values)
        

####################################################### 

if __name__ == '__main__':
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)
    main()