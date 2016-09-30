import argparse
import numpy as np
import pickle
from sklearn import linear_model
from sklearn.externals import joblib

MODEL_AGE = "model/logreg_age.pkl"
MODEL_GEN = "model/logreg_gen.pkl"
#PREDICTION = "predicted.csv"

age_list = ["xx-24", "25-34", "35-49", "50-xx"]

pages_list = ()
pages_index = dict()
logreg_age = ""
logreg_gen = ""

def read_trained_model():
	global pages_list, pages_index, logreg_age, logreg_gen
	fr = open(MODEL_AGE + "_dump", 'r')
	pages_list = pickle.load(fr)
	fr.close()

	i = 0
	for pageid in pages_list:
		pages_index[pageid] = i
		i += 1 

	logreg_gen = joblib.load(MODEL_GEN)
	logreg_age = joblib.load(MODEL_AGE)

def get_prediction(userid, page_liked):
	X = np.ndarray(shape=(1, len(pages_list)), dtype=int)
	X.fill(0)

	for pageid in page_liked:
		if pageid in pages_index:
			index = pages_index[pageid]
			X[0][index] = 1

	#print userid, page_liked
	age_pred = logreg_age.predict(np.c_[X])[0]
	age_pred = age_list[age_pred]

	gen_pred = logreg_gen.predict(np.c_[X])[0]
	gen_pred = float(gen_pred)

	return age_pred, gen_pred

	# avg_ope = "3.91"
	# avg_con = "3.45"
	# avg_ext = "3.48"
	# avg_agr = "3.58"
	# avg_neu = "2.732"
	# fw.write(userid+ "," + age_pred + "," + gen_pred + "," + avg_ope + "," + avg_con + "," + avg_ext + "," + avg_agr + "," + avg_neu + "\n")

def start(testDataPath, userid):
	# parser = argparse.ArgumentParser(usage = "tcss555 -i input_dir -o output_dir")
	# parser.add_argument('-i', required = True)
	# args = parser.parse_args()
	# inputfile = args.i

	inputfile = testDataPath + "/relation/relation.csv"    #relation test

	read_trained_model()

	fr = open(inputfile, "r")
	#fw = open(PREDICTION, 'w')
	fr.readline()

	user_pages_liked = {}
	#user:pages liked

	for line in fr:
		row = line.rstrip().split(",")
		uid = row[1]
		pgid = row[2]

		if uid not in user_pages_liked:
			user_pages_liked[uid] = set()
			user_pages_liked[uid].add(pgid)
		else:
			user_pages_liked[uid].add(pgid)

	# page_liked = set()
	# userid = ''

	# for line in fr:
	# 	row = line.rstrip().split(",")
	# 	if row[1] != userid:
	# 		if userid != '':
	# 			## not the first input
	# 			get_prediction(userid, page_liked, fw)
	# 		userid = row[1]
	# 		page_liked = set()
	# 		page_liked.add(row[2])
	# 	else:
	# 		page_liked.add(row[2])

	# find neighbour of last

	age_pred, gen_pred = get_prediction(userid, user_pages_liked[userid])
	fr.close()
	#fw.close()

	return age_pred, gen_pred

# if __name__ == '__main__':
# 	main()
