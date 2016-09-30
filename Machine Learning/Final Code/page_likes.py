#!/usr/bin/python
import argparse
import numpy as np
from collections import Counter

PROFILE = "/data/training/profile/profile.csv"
RELATION_TRAIN = "/data/training/relation/relation.csv"
#OUTPUT_PATH = ""
#RELATION_TEST = testfile
#PROFILE_TEST = ""
#PREDICTION = "predicted.csv"
HIGH_THRESHOLD = 50
LOW_THRESHOLD = 0

# dict of pageid vs userid that have liked this page.
pages_likes = dict()
profile_data = dict()
#pages_model = dict()

def read_pages(inputfile):
	#pages : {users who liked that page}
	fr = open(inputfile, 'r')
	fr.readline()

	for line in fr:
		row = line.rstrip().split(",")
		userid = row[1]
		pageid = row[2]
		if pageid not in pages_likes:
			pages_likes[pageid] = set()
		pages_likes[pageid].add(userid)

	#print pages_likes
	fr.close()

def get_bucket(age):
    age = float(age)
    if age < 25.0:
        return "xx-24"
    elif age < 35.0:
        return "25-34"
    elif age < 50.0:
        return "35-49"
    return "50-xx"

def read_user_profile():
	#uid : [profile data]
	fr = open(PROFILE,"r")
	fr.readline()

	for line in fr:
		row = line.rstrip().split(",")
		del row[0]
		uid = row[0]
		del row[0]

		profile_data[uid] = row
	fr.close()
	#print profile_data

def get_prediction(userid, user_pages_liked):
	age = list()
	gender = list()
	ope = list()
	con = list()
	ext = list()
	agr = list()
	neu = list()
	matched_users = set()

	for pageid in user_pages_liked:
		if pageid in pages_likes and len(pages_likes[pageid]) < HIGH_THRESHOLD and len(pages_likes[pageid]) > LOW_THRESHOLD:
			matching_userids = pages_likes[pageid]
			for matching_user in matching_userids:
				user_row = profile_data[matching_user]
				age.append(get_bucket(user_row[0]))
				gender.append(float(user_row[1]))
				ope.append(float(user_row[2]))
				con.append(float(user_row[3]))
				ext.append(float(user_row[4]))
				agr.append(float(user_row[5]))
				neu.append(float(user_row[6]))

	#print age
	if len(age) > 0:
		common_age = Counter(age).most_common(1)[0][0]
		common_gen = Counter(gender).most_common(1)[0][0]
		avg_ope = np.mean(ope)
		avg_con = np.mean(con)
		avg_ext = np.mean(ext)
		avg_agr = np.mean(agr)
		avg_neu = np.mean(neu)
	else:
		common_age = "xx-24"
		common_gen = "1.0"
		avg_ope = "3.91"
		avg_con = "3.45"
		avg_ext = "3.48"
		avg_agr = "3.58"
		avg_neu = "2.732"


	return common_age,common_gen, avg_ope, avg_neu, avg_ext, avg_agr, avg_con

	# fw.write(userid+ "," + common_age + "," + common_gen + "," + avg_ope + ","
	# 	+ avg_con + "," + avg_ext + "," + avg_agr + "," + avg_neu + "\n")

	#writeXMLoutput(userid, common_gen, common_age, avg_ope, avg_con, avg_ext, avg_agr, avg_neu)

# def writeXMLoutput(userID, gender, age, ope, con, ext, agr, neu) :
# 	fw = open(OUTPUT_PATH + "/" + userID + ".xml", "w")	
# 	fw.write("<User id = \"" + userID + "\"\n" 
# 		+ "age_group = \"" + age + "\"\n" 
# 		+ "gender = \"" + gender + "\"\n" 
#         + "open = \"" + ope + "\"\n"
#         + "conscientious = \"" + con + "\"\n"
#         + "extrovert = \"" + ext + "\"\n" 
#         + "agreeable = \"" + agr + "\"\n" 
#         + "neurotic = \"" + neu + "\"\n" 
# 	+ "/>")
# 	fw.close()


def start(testDataPath, userid):
	#global OUTPUT_PATH

	inputfile = RELATION_TRAIN
	testfile = testDataPath + "/relation/relation.csv"    #relation test
	#OUTPUT_PATH = outputPath

	read_pages(inputfile)
	read_user_profile()

	fr = open(testfile, "r")
	fr.readline()
	#fw = open(PREDICTION, 'w')

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
	# 			get_prediction(userid, page_liked)
	# 		userid = row[1]
	# 		page_liked = set()
	# 		page_liked.add(row[2])
	# 	else:
	# 		page_liked.add(row[2])

	age, gender, ope, neu, ext, agr, con = get_prediction(userid, user_pages_liked[userid])
	fr.close()
	#fw.close()
	return age,gender, ope, neu, ext, agr, con

