#!/usr/bin/python
#Script to submit ranking of next possible element in sequence- to spice website

import numpy as np
from hmmlearn import hmm
from sklearn.metrics import accuracy_score
from collections import Counter
import sys, os
import operator

# State the problem number
problem_number = '15'

# and the user id (given during registration)
user_id = '48'

# name of this submission (only letters, numbers and _)
name = "15_HMM"

TRAIN = 'spice_data/spice_public_train/15_spice_train.txt'
#spice_data/train/0.spice.train.80.txt
#train_spice.txt
TEST = 'spice_data/spice_public_test/15_public_test.txt'
#spice_data/test/0.spice.test.txt
#test_spice.txt

NUM_HIDDEN_STATES = 10
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

    fr = open(TRAIN ,"r")
    data_info = fr.readline().rstrip().split(" ")
    num_samples = int(data_info[0])
    num_states = int(data_info[1]) + 1

    for line in fr:
        row = line.rstrip().split(" ")
        seq_len = np.array(int(row[0]) + 1, dtype=np.int16)
        seq = row[1:]
        if(int(row[0]) != len(seq)):
            print "ERROR in seq length values:", row

        if(len(seq) == 1):
            continue

        #append -1 to the end of sequence and increase number of classes by 1
        seq.append("-1")

        X.extend(seq)

        #append the length of sequences into a list
        lengths.append(seq_len)

    #print "lengths:", len(lengths)
    #print "X:", len(X)


    X = np.array(X, dtype=np.int16)
    X = np.reshape(X, (-1, 1))
    fr.close()

    print "Fitting model........."
    #trains the Gaussian HMM model
    model = hmm.GaussianHMM(n_components=NUM_HIDDEN_STATES,n_iter=NUM_ITER).fit(X, lengths)

    return model


#Get first prefix
def get_first_prefix(test_file):
    #get the only prefix in test_file
    fr = open(test_file,"r")
    prefix = fr.readline()
    fr.close()
    return prefix


#Testing the model
def predict_test_data(model,first_prefix):

    actual_values = []
    predicted_values = []
    probabilities = {}
    ranking = []

    row = first_prefix.rstrip().split(" ")
    seq_len = int(row[0])
    seq = row[1:seq_len]

    #Converting the sequence into 2D array
    seq = np.array(seq, dtype=np.int16)
    seq = np.reshape(seq, (-1, 1))

    for index in range(-1,(num_states - 1)):
        #Get probability of sequence with last element 0
        X = np.concatenate((seq,[[index]]))
        prob = model.decode(X,algorithm='viterbi')[0]
        probabilities[index] = prob
        X.fill(0)


    #get value which gives max probability if it is at the end of the prefix, and predict that value
    sorted_prob = sorted(probabilities.items(), key=operator.itemgetter(1))

    count = 0
    #iterate on reversed list
    for index in sorted_prob[::-1]:
        count += 1
        ranking.append(index[0])
        if(count == 5):
            break

    rank_string = " ".join(str(v) for v in ranking)

    return rank_string


def formatString(string_in):
    return string_in.strip().replace(" ", "%20")


def main():
    # learn the model
    print ("Start Learning")
    model = train_model()
    print ("End of learning phase")

    # get the test first prefix: the only element of the test set
    first_prefix = get_first_prefix(TEST)

    # get the next symbol ranking on the first prefix
    print ("Start Predicting")
    ranking = predict_test_data(model,first_prefix)
    print ("End of predicting phase")

    print("Prefix number: 1 Prefix: " + first_prefix + " Ranking: " + ranking)

    # transform ranking to follow submission format (with %20 between symbols)
    ranking = formatString(ranking)

    # transform the first prefix to follow submission format
    first_prefix = formatString(first_prefix)

    # create the url to submit the ranking
    url_base = 'http://spice.lif.univ-mrs.fr/submit.php?user=' + user_id +\
           '&problem=' + problem_number + '&submission=' + name + '&'
    url = url_base + 'prefix=' + first_prefix + '&prefix_number=1' + '&ranking=' +\
      ranking


    # Get the website answer for the first prefix with this ranking using this submission name
    try:
        # Python 2.7
        import urllib2 as ur
        orl2 = True
    except:
        #Python 3.4
        import urllib.request as ur
        orl2 = False


    response = ur.urlopen(url)
    content = response.read()
    if not orl2:
        # Needed for python 3.4...
        content= content.decode('utf-8')

    list_element = content.split()
    head = str(list_element[0])

    prefix_number = 2

    while(head != '[Error]' and head != '[Success]'):
        # Get rid of Line feed
        prefix = content[:-1]
        
        # Get the ranking
        ranking = predict_test_data(model, prefix)
        
        #print("Prefix number: " + str(prefix_number) + " Ranking: " + ranking + " Prefix: " + prefix)
        
        # Format the ranking
        ranking = formatString(ranking)

        # create prefix with submission needed format
        prefix = formatString(prefix)

        # Create the url with your ranking to get the next prefix
        url = url_base + 'prefix=' + prefix + '&prefix_number=' +\
            str(prefix_number) + '&ranking=' + ranking

        # Get the answer of the submission on current prefix
        response = ur.urlopen(url)
        content = response.read()

        if not orl2:
            # Needed for python 3.4...
            content= content.decode('utf-8')

        list_element = content.split()

        # modify head in case it is finished or an erro occured
        head = str(list_element[0])

        # change prefix number
        prefix_number += 1


    # Post-treatment
    # The score is the last element of content (in case of a public test set)
    print(content)

    list_element = content.split()
    score = (list_element[-1])
    print(score)

#########################################

if __name__ == '__main__':
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)
    main()