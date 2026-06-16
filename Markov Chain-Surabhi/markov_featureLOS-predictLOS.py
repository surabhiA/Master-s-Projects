#!/usr/bin/python
import numpy as np
from sklearn.metrics import accuracy_score

#Using OSH-SPICE files to predict next LOS using LOS as feature
TRAIN = 'data/four_or_more/CHF_30DAY_SPICE.txt'
#data/four_or_more/ConvertedFormat/CHF_LOS_SPICE.txt

TEST = 'data/four_or_more/private/CHF_30DAY_SPICE_TEST.txt'
#data/four_or_more/ConvertedFormat/private/CHF_LOS_SPICE_TEST.txt

#############################
def get_prediction(patient_seq, model, test_pred_labels, num_symbols):

	seq = []

	#Record probabilities for each symbol attached to the end of the sequence, in an array
	sym_prob = [0] * num_symbols

	for symbol in range(num_symbols):
		#Copy the patient sequence to new array
		seq = patient_seq[:]

		#Append the symbol
		seq.append(symbol)

		#Record the last transition
		row = int(seq[len(seq) - 2])
		col = int(seq[len(seq) - 1])

		#sym_prob[0] *= model[0][row][col]
		#The probability of that symbol will simply be the probability of that transition in that symbol matrix
		sym_prob[symbol] = model[symbol][row][col]

	#Get the max probability symbol2
	max_prob = max(sym_prob)
	pred_value = sym_prob.index(max_prob)
	test_pred_labels.append(pred_value)

#############################
def gen_seq_test_data(fr, model, num_symbols):

    test_orig_labels = []
    target_var = 0
    patient_seq = []
    test_pred_labels = []

    for line in fr:
        row = line.rstrip().split(" ")
    	del row[0]
    	target_var = int(row[len(row) - 1])

    	#Remove the last symbol(Which is to be predicted)
    	del row[len(row) - 1]
    	patient_seq = row
    	test_orig_labels.append(target_var)

    	#Get prediction for each patient
    	get_prediction(patient_seq, model, test_pred_labels, num_symbols)

    return test_orig_labels, test_pred_labels

#############################
def lap_smoothing(model, num_transitions, num_symbols):

	num_possible_comb = num_symbols * num_symbols

	for modelX in model:
		for ele in np.nditer(modelX, op_flags=['readwrite']):
			ele[...] = float(ele + 1) / (num_transitions + num_possible_comb)

#############################
def fill_matrix(patient_seq, target_var, model, num_transitions):
    
    #iterate till last but one symbol, since we are using transitions. The last element will have no transition.
    for index in range(len(patient_seq) - 1):
        #Record the row and column according to the transition
        row = int(patient_seq[index])
        col = int(patient_seq[index + 1])

        #Record the transition in the matrix which corresponds to the target variable.
        model[target_var][row][col] += 1
        num_transitions += 1

    return num_transitions

#############################
def gen_seq(fr, model):
    
    patient_seq = []
    target_var = 0
    num_transitions = 0

    for line in fr:
    	row = line.rstrip().split(" ")
    	del row[0]
    	patient_seq = row
    	target_var = int(row[len(row) - 1])

    	#Fill the matrix for each patient
    	num_transitions = fill_matrix(patient_seq, target_var, model, num_transitions)

    return num_transitions
    	
#############################
def gen_model(num_symbols):

	#Generate an array of empty matrices which will be the model (In our case a 3D array is generated)
	model = np.zeros((num_symbols,num_symbols,num_symbols))
	return model

#############################
def main():

	fr = open(TRAIN,"r")
	header = fr.readline().rstrip().split(" ")

	#The number of symbols obtained from the header
	num_symbols = int(header[1])

	#This function generates an empty array of matrices(Number of matrices equal to number of symbols).
	model = gen_model(num_symbols)
	#print model
	
	fr.seek(0)
	fr.readline()

	#This function trains the model
	num_transitions = gen_seq(fr, model)
	#print model

	fr.close()

	#This function performs Laplace smoothing on the trained model.
	lap_smoothing(model, num_transitions, num_symbols)
	print model

	#################

	fr = open(TEST, "r")
	fr.readline()

	#This function predicts the next symbol.
	test_orig_labels, test_pred_labels = gen_seq_test_data(fr, model, num_symbols)
	fr.close()

	print accuracy_score(test_orig_labels, test_pred_labels)

if __name__ == '__main__':
    main()