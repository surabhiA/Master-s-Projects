import argparse
import numpy as np
from sklearn import linear_model
from sklearn.externals import joblib
import pickle

PROFILE = "data/training/profile/profile.csv"
#PROFILE = "profile.csv"
MODEL_AGE = "model/logreg_age.pkl"
MODEL_GEN = "model/logreg_gen.pkl"
HIGH_THRESHOLD = 200
LOW_THRESHOLD = 4

pages_list = list()
pages_index = dict()
profile_data = dict()

def get_bucket(age):
	age = float(age)
	if age < 25.0:
		return 0
	elif age < 35.0:
		return 1
	elif age < 50.0:
		return 2
	return 3

def read_user_profile():
	fr = open(PROFILE,"r")
	fr.readline()

	for line in fr:
		row = line.rstrip().split(",")
		del row[0]
		uid = row[0]
		del row[0]

		row[0] = get_bucket(row[0])
		profile_data[uid] = row
	fr.close()
	#print profile_data

def build_all_pages(inputfile):
	global pages_list
	fr = open(inputfile, "r")
	fr.readline()

	pages_dict = dict()
	for line in fr:
		row = line.rstrip().split(",")
		userid = row[1]
		pageid = row[2]
		if pageid not in pages_dict:
			pages_dict[pageid] = set()
		pages_dict[pageid].add(userid)

	fr.close()

	i = 0
	for pageid in pages_dict:
		likes = len(pages_dict[pageid])
		if likes <= HIGH_THRESHOLD and likes >= LOW_THRESHOLD:
			pages_list.append(pageid)
			pages_index[pageid] = i
			i += 1

	#pages_list = list(pages_set)
	#i = 0
	#for pageid in pages_list:
	#	pages_index[pageid] = i
	#	i += 1
	fw = open(MODEL_AGE + '_dump', 'w')
	pickle.dump(pages_list, fw)
	#print pages_list, pages_index
	fw.close()
	print "BUILDING of pages done"

def generate_model_input(inputfile):
	fr = open(inputfile, 'r')
	fr.readline()

	userid = ''
	page_liked = set()
	model_input = dict()
	for line in fr:
		row = line.rstrip().split(",")
		if row[1] != userid:
			if userid != '':
				## not the first input
				model_input[userid] = page_liked
			userid = row[1]
			page_liked = set()
			page_liked.add(row[2])
		else:
			page_liked.add(row[2])
	model_input[userid] = page_liked	
	fr.close()
	print "GENERATED model input"
	return model_input

def train_model(model_input):
	num_users = len(model_input)
	num_pages = len(pages_list)

	#print model_input
	#print num_users, num_pages
	X = np.ndarray(shape=(num_users, num_pages), dtype=int)
	X.fill(0)
	Y_age = np.ndarray(shape=(num_users, ), dtype=int)
	Y_gen = np.ndarray(shape=(num_users, ), dtype=int)

	i = 0
	for userid in model_input:
		user_age = profile_data[userid][0]
		user_gen = float(profile_data[userid][1])
		for liked_page in model_input[userid]:
			if liked_page in pages_index:
				idx = pages_index[liked_page]
				X[i][idx] = 1
		Y_age[i] = user_age
		Y_gen[i] = user_gen
		i += 1

	#print X, Y, model_input.keys(), pages_list
	logreg_age = linear_model.LogisticRegression(n_jobs=4)
	logreg_age.fit(X, Y_age)

	logreg_gen = linear_model.LogisticRegression(n_jobs=4, class_weight='balanced')
	logreg_gen.fit(X, Y_gen)

	print "TRAINED model"
	return logreg_age, logreg_gen

def main():
	#Takes input in the form required
	parser = argparse.ArgumentParser(usage = "tcss555 -i input_file")
	parser.add_argument('-i', required = True)

	args = parser.parse_args()
	inputfile = args.i
	read_user_profile()
	build_all_pages(inputfile)
	model_input = generate_model_input(inputfile)
	(logreg_age, logreg_gen) = train_model(model_input)
	joblib.dump(logreg_age, MODEL_AGE)
	joblib.dump(logreg_gen, MODEL_GEN)

if __name__ == '__main__':
	main()