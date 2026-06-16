#!/usr/bin/python
import numpy as np
from sklearn.metrics import accuracy_score
import sys

#Please provide the problem you are trying to predict at the commandline- los/cost/thirtyday

TRAIN = '../OSHPD-MASTER-AY2016-2017/sourcedata/OSHPD_ALLCAUSE_PUBLIC_KNOWNCOST_BUCKET_NOLAST.csv'
#../OSHPD-MASTER-AY2016-2017/sourcedata/OSHPD_CHF_PUBLIC_KNOWNCOST_BUCKET_NOLAST.csv

TEST = '../OSHPD-MASTER-AY2016-2017/sourcedata/OSHPD_ALLCAUSE_PRIVATE_KNOWNCOST_BUCKET_NOLAST.csv'
#../OSHPD-MASTER-AY2016-2017/sourcedata/OSHPD_CHF_PRIVATE_KNOWNCOST_BUCKET_NOLAST.csv

num_symbols_los = 6
num_symbols_sevcode = 3

pid_index = 0
los_index = 0
sevcode_index = 0
pred_problem_index = 0
conversion_req = False

map_rows_cols = {}

#############################
def get_prediction(patient_seq, model, test_pred_labels, num_symbols_pred):

	#Record probabilities for each symbol attached to the end of the sequence, in an array
	sym_prob = [0] * num_symbols_pred

	for symbol in range(num_symbols_pred):

		#Record the last transition
		row_transn = patient_seq[len(patient_seq) - 1]
		row_index = map_rows_cols[row_transn]
		sym_prob[symbol] = max(model[symbol][row_index])

	#Get the max probability symbol2
	max_prob = max(sym_prob)
	pred_value = sym_prob.index(max_prob)
	test_pred_labels.append(pred_value)

#############################
def gen_seq_test_data(fr, model, num_symbols_pred):

	test_orig_labels = []
	target_var = 0
	patient_seq = []
	test_pred_labels = []
	uid = ""

	for line in fr:
		row = line.rstrip().split(",")
		pid = row[pid_index]

		los_value = int(row[los_index]) - 1
		sevcode_value = row[sevcode_index]
		transition = str(los_value) + "-" + sevcode_value

		if(pid == uid):
			if(conversion_req == True):
				target_var = int(row[pred_problem_index]) - 1
			else:
				target_var = int(row[pred_problem_index])

			patient_seq.append(transition)

		elif(uid == ""):
			uid = pid
			if(conversion_req == True):
				target_var = int(row[pred_problem_index]) - 1
			else:
				target_var = int(row[pred_problem_index])

			patient_seq.append(transition)

		else:
			if(len(patient_seq) >= 4):
				test_orig_labels.append(target_var)
				del patient_seq[len(patient_seq) - 1]
				get_prediction(patient_seq, model, test_pred_labels, num_symbols_pred)

			uid = pid
			if(conversion_req == True):
				target_var = int(row[pred_problem_index]) - 1
			else:
				target_var = int(row[pred_problem_index])

			patient_seq = []
			patient_seq.append(transition)

	if(len(patient_seq) >= 4):
		test_orig_labels.append(target_var)
		del patient_seq[len(patient_seq) - 1]
		get_prediction(patient_seq, model, test_pred_labels, num_symbols_pred)

	return test_orig_labels, test_pred_labels

#############################
def lap_smoothing(model, num_transitions):

	num_possible_comb = num_symbols_los * num_symbols_sevcode

	for modelX in model:
		for ele in np.nditer(modelX, op_flags=['readwrite']):
			ele[...] = float(ele + 1) / (num_transitions + num_possible_comb)

#############################
def fill_matrix(patient_seq, target_var, model, num_transitions):
	# print patient_seq
	# print "tar", target_var

	#iterate till last but one symbol, since we are using transitions. The last element will have no transition.
	for i in range(len(patient_seq) - 1):
		row_transn = patient_seq[i]
		col_transn = patient_seq[i + 1]

		# print "rowtrans:", row_transn
		# print "coltrans:", col_transn

		row_index = map_rows_cols[row_transn]
		col_index = map_rows_cols[col_transn]

		# print "row", row_index
		# print "col", col_index

		#Record the transition in the matrix which corresponds to the target variable.
		model[target_var][row_index][col_index] += 1
		num_transitions += 1

	return num_transitions

#############################
def gen_seq(fr, model):
    
	patient_seq = []
	target_var = 0
	num_transitions = 0
	uid = ""

	for line in fr:
		row = line.rstrip().split(",")
		pid = row[pid_index]

		#Convert 1-6 buckets to 0-5
		los_value = int(row[los_index]) - 1
		sevcode_value = row[sevcode_index]
		transition = str(los_value) + "-" + sevcode_value

		if(pid == uid):
			if(conversion_req == True):
				target_var = int(row[pred_problem_index]) - 1
			else:
				target_var = int(row[pred_problem_index])

			patient_seq.append(transition)

		elif(uid == ""):
			uid = pid
			if(conversion_req == True):
				target_var = int(row[pred_problem_index]) - 1
			else:
				target_var = int(row[pred_problem_index])

			patient_seq.append(transition)

		else:
			if(len(patient_seq) >= 4):
				num_transitions = fill_matrix(patient_seq, target_var, model, num_transitions)

			uid = pid
			if(conversion_req == True):
				target_var = int(row[pred_problem_index]) - 1
			else:
				target_var = int(row[pred_problem_index])

			patient_seq = []
			patient_seq.append(transition)

	if(len(patient_seq) >= 4):
		num_transitions = fill_matrix(patient_seq, target_var, model, num_transitions)
    
	return num_transitions

#############################
def map_model():
	global map_rows_cols

	i = 0
	for los in range(num_symbols_los):
		for sevcode in range(num_symbols_sevcode):
			key = str(los) + "-" + str(sevcode)
			map_rows_cols[key] = i
			i = i+1

##############################
def gen_model(num_symbols_pred):

	#Generate an array of empty matrices which will be the model (In our case a 3D array is generated)
	model = np.zeros((num_symbols_pred,num_symbols_los*num_symbols_sevcode,num_symbols_los*num_symbols_sevcode))
	return model

#############################
def main():
	global pid_index, los_index, sevcode_index, pred_problem_index, conversion_req

	fr = open(TRAIN,"r")
	column_list = fr.readline().rstrip().split(",")
	pid_index = column_list.index("PID")
	los_index = column_list.index("LOS_b")
	sevcode_index = column_list.index("msdrg_severity_ill")
	cost_index = column_list.index("cost_b")
	thirtyday_index = column_list.index("thirtyday")

	if(len(sys.argv) < 2):
		print "Please provide command line argument.This will be the problem you want to predict.Give one of(Case sensitive): los/cost/thirtyday"
		return

	if(sys.argv[1] == "los"):
		num_symbols_pred = 6
		pred_problem_index = los_index
		conversion_req = True
	elif(sys.argv[1] == "cost"):
		num_symbols_pred = 4
		pred_problem_index = cost_index
		conversion_req = True
	elif(sys.argv[1] == "thirtyday"):
		num_symbols_pred = 2
		pred_problem_index = thirtyday_index
		conversion_req = False
	else:
		print "Please give commandline arg out of: los/cost/thirtyday"
		return


	#This function generates an empty list of dictionaries(Number of dictionaries equal to number of symbols).
	model = gen_model(num_symbols_pred)
	#print model

	map_model()
	#print map_rows_cols

	#This function trains the model
	num_transitions = gen_seq(fr, model)
	#print model[2][5]
	#print "num transitions:", num_transitions

	fr.close()

	#This function performs Laplace smoothing on the trained model.
	lap_smoothing(model, num_transitions)
	#print model[2][5]

	#################

	fr = open(TEST, "r")
	fr.readline()

	#This function predicts the next symbol.
	test_orig_labels, test_pred_labels = gen_seq_test_data(fr, model, num_symbols_pred)
	fr.close()

	#print "Orig labels:",test_orig_labels
	#print "pred labels;", test_pred_labels

	print "Accuracy:",accuracy_score(test_orig_labels, test_pred_labels)

if __name__ == '__main__':
    main()