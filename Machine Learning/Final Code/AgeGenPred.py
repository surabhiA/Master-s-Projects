#!/usr/bin/python
import nltk
import csv
import sklearn

from nltk import FreqDist
from nltk import word_tokenize
from nltk.corpus import stopwords
from nltk import NaiveBayesClassifier
from nltk import classify
from sklearn.naive_bayes import MultinomialNB
from nltk.classify.scikitlearn import SklearnClassifier

############routines to train the model################

#Created the dictionary of the words for each user
def getDict(filename) :
	fileComment = open(filename , 'rU')
	# our data is in latin-1 format
	rawData = fileComment.read().decode('latin-1')
	tokens = word_tokenize(rawData)

	# remove the common and non-alpha tokens
	ignoredWords = stopwords.words('english')
	filterNoAlpha = [i.lower()for i in tokens if i.isalpha()]
	filtered_words = [i for i in filterNoAlpha if i not in ignoredWords]

	fdist1 = FreqDist(filtered_words)
	final = fdist1.most_common(200)

	fileComment.close()
	return dict(final)

#This function gets age bracket of the user for classification
def getAgeBracket(age) :
	age = float(age)
	if age >= 0.0 and age <= 24.0:
		return 'xx-24'
	elif age >= 25.0 and age <= 34.0:
		return '25-34'
	elif age >= 35.0 and age <= 49.0:
		return '35-49'
	else:
		return '50-xx'

#Creates the tarining data 
#By appending the dict and label (Age or gender)
def getFeatureSets(profileFile, commentDir) :
	profileReader = csv.reader(profileFile)
	
	# skip headers
	headersProfile = next(profileReader)

	firstRec = next(profileReader)
	data = getDict(commentDir + firstRec[1] + ".txt")

	featuresetsAge = [(data, getAgeBracket(firstRec[2]))]
	featuresetsSex = [(data, firstRec[3])]

	for row in profileReader :
		data = getDict(commentDir + row[1] + ".txt")

		featuresetsAge.append((data, getAgeBracket(row[2])))
		featuresetsSex.append((data, row[3]))

	return featuresetsSex, featuresetsAge

#Trains the naive bayes classifier

def trainClassifier(featuresets, trainStart) :
	classifier = SklearnClassifier(MultinomialNB())
	classifier.train(featuresets[trainStart:])
	return classifier

#Trains the model by using the test and training data 
# In this we are training on the complete dataset
def trainAgeGender(testSize) :
	profileFile = open("/data/training/profile/profile.csv", "rU")

	tdSex, tdAge = getFeatureSets(profileFile, "/data/training/text/")
	cfSex = trainClassifier(tdSex, testSize)
	cfAge= trainClassifier(tdAge, testSize)

	profileFile.close()
	return cfSex, cfAge
