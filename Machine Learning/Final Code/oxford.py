#!/usr/bin/python

import csv
import sklearn
import argparse
import numpy as np
import pandas as pd
import warnings
import math

from sklearn.ensemble import RandomForestClassifier
from sklearn import linear_model
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error
from nltk.classify.scikitlearn import SklearnClassifier
from sklearn.neighbors import KNeighborsClassifier


def getImages():

    warnings.simplefilter("ignore")

    firstPartOfDir = '/data/training/'
    oxfordFile = open(firstPartOfDir + '/oxford.csv')
    oxfordReader = csv.reader(oxfordFile)
    ncol = len(next(oxfordReader))  ## use header row to find number of columns

        # take out first 2 columns (Id nums) and last 3 (contain neg's and all 0s)
    oxfordFeatures = np.empty(shape=(7900, ncol - 5))
        #oxfordFeaturesTest = np.empty(shape=(1915, ncol - 5))
    ageLabel = []
    genderLabel = []
        #ageLabelTest = []
        #genderLabelTest = []
        
    opeLabel = []
        #opeLabelTest = []
    conLabel = []
        #conLabelTest = []
    extLabel = []
        #extLabelTest = []
    agrLabel = []
        #agrLabelTest = []
    neuLabel = []
        #neuLabelTest = []
    modelList = []        

    rowNum = 0
    for row in oxfordReader:
        userID = row[0]
        age, gender, ope, con, ext, agr, neu = matchRow(userID, firstPartOfDir)
        features = row[2:-3]
        if rowNum < 7900:
            oxfordFeatures[rowNum] = features[:]
            ageLabel.append(age)
            genderLabel.append(gender)
            opeLabel.append(ope)
            conLabel.append(con)
            extLabel.append(ext)
            agrLabel.append(agr)
            neuLabel.append(neu)
                #elif rowNum < 7915:
                #        oxfordFeaturesTest[rowNum - 6000] = features[:]
                #        ageLabelTest.append(age)
                #        genderLabelTest.append(gender)
                #        opeLabelTest.append(ope)
                #        conLabelTest.append(con)
                #        extLabelTest.append(ext)
                #        agrLabelTest.append(agr)
                #        neuLabelTest.append(neu)
        else:
            break
        rowNum = rowNum + 1

	# create random forest classifier for age
    clfRFage = RandomForestClassifier(n_estimators=10)
    clfRFage = clfRFage.fit(oxfordFeatures, ageLabel)
        #scoreAge = clfRFage.score(oxfordFeaturesTest, ageLabelTest)
        #print("RF Age score: ", scoreAge)
        #ageListRF = clfRFage.predict(oxfordFeaturesTest)
    modelList.append(clfRFage)

        # create random forest classifier for gender
    clfRFgender = RandomForestClassifier(n_estimators=10)
    clfRFgender = clfRFgender.fit(oxfordFeatures, genderLabel)
        #scoreGender = clfRFgender.score(oxfordFeaturesTest, genderLabelTest)
        #print("RF Gender score: ", scoreGender)
        #genderListRF = clfRFgender.predict(oxfordFeaturesTest)
    modelList.append(clfRFgender)

        # create knn(5) classifier for gender
        #clfKNNgender = KNeighborsClassifier(n_neighbors=5)
        #clfKNNgender = clfKNNgender.fit(oxfordFeatures, genderLabel)
        #scoreKNNgender = clfKNNgender.score(oxfordFeaturesTest, genderLabelTest)
        #print("KNN Gender score: ", scoreKNNgender)

        # create knn(5) classifier for age
        #clfKNNage = KNeighborsClassifier(n_neighbors=5)
        #clfKNNage = clfKNNage.fit(oxfordFeatures, ageLabel)
        #scoreKNNage = clfKNNage.score(oxfordFeaturesTest, ageLabelTest)
        #print("KNN Age score: ", scoreKNNage)

        # make lists out of numpy arrays to change values from floats to strings
    oxfordFeatList = oxfordFeatures.tolist()
        #oxfordFeatTestList = oxfordFeaturesTest.tolist()
    for item in oxfordFeatList:
        for j in range(len(item)):
            item[j] = float(item[j])
        #for item in oxfordFeaturesTest:
        #        for j in range(len(item)):
        #                item[j] = float(item[j])
        
        # create lin reg classifier for ope
    regOpe = linRegr(opeLabel, oxfordFeatList)
        #rms = math.sqrt(mean_squared_error(opeLabelTest, regOpe.predict(oxfordFeatTestList)))
        #print("Ope RMSE: ", rms)
    modelList.append(regOpe)
        
	# create lin reg classifier for neu
    regNeu = linRegr(neuLabel, oxfordFeatList)
        #rms = math.sqrt(mean_squared_error(neuLabelTest, regNeu.predict(oxfordFeatTestList)))
        #print("Ope RMSE: ", rms)
    modelList.append(regNeu)

	# create lin reg classifier for ext
    regExt = linRegr(extLabel, oxfordFeatList)
        #rms = math.sqrt(mean_squared_error(extLabelTest, regExt.predict(oxfordFeatTestList)))
        #print("Ext RMSE: ", rms)
    modelList.append(regExt)

	# create lin reg classifier for agr
    regAgr = linRegr(agrLabel, oxfordFeatList)
        #rms = math.sqrt(mean_squared_error(agrLabelTest, regAgr.predict(oxfordFeatTestList)))
        #print("Agr RMSE: ", rms)
    modelList.append(regAgr)

        # create lin reg classifier for con
    regCon = linRegr(conLabel, oxfordFeatList)
        #rms = math.sqrt(mean_squared_error(conLabelTest, regCon.predict(oxfordFeatTestList)))
        #print("Con RMSE: ", rms)
    modelList.append(regCon)
        
        # need to return all models that we want to use
    return modelList


# This function creates a lin reg model
def linRegr(label, oxfordFeatList):
    regr = LinearRegression()
    for item in range(len(label)):
        label[item] = float(label[item])
        #for item in range(len(labelTest)):
        #        labelTest[item] = float(labelTest[item])
    regr = regr.fit(oxfordFeatList, label)
    return regr
        
        

# This function finds the row in profile to match the row from oxford during training
def matchRow(userID, firstPartOfDir):
    profileFile = open(firstPartOfDir + '/profile/profile.csv')
    profileReader = csv.reader(profileFile)
    headersProfile = next(profileReader)
    for row in profileReader:
        if row[1] == userID:
            age = getAgeBracket(row[2])
            gender = row[3]
            ope = row[4]
            con = row[5]
            ext = row[6]
            agr = row[7]
            neu = row[8]
            break;
    return age, gender, ope, con, ext, agr, neu

# This function finds row in oxford to match userID for test users
def getMatch(userID, firstPartOfDir):
    oxfordFile = open(firstPartOfDir + '/oxford.csv')
    oxfordReader = csv.reader(oxfordFile)
    oxfordHeaders = next(oxfordReader)
    for row in oxfordReader:
        if row[0] == userID:
            features = row[2:-3]
            return(features)


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



def predictTestData(testDataPath, userID, modelList):
        #newFirstPartOfDir = testDataPath
    valueList = []
        #profileFile = open(newFirstPartOfDir + 'profile/profile.csv')
        #publicReader = csv.reader(profileFile)
        #publicHeadersProfile = next(publicReader)
        #count = 0
        #for row in publicReader:
                #userID = row[1]
                #count = count + 1
    userFeatures = getMatch(userID, testDataPath)

    if userFeatures == None:
        gender = list((1.0, ))
        age = list(('xx-24', ))
        ope = list((3.9087, ))
        con = list((3.4456, ))
        ext = list((3.4869, ))
        agr = list((3.5839, ))
        neu = list((2.7324, ))
    else:
        age = modelList[0].predict(userFeatures)
        # gender = float(modelList[1].predict(userFeatures))
        for j in range(len(userFeatures)):
            userFeatures[j] = float(userFeatures[j])

        gender = modelList[1].predict(userFeatures)
        ope = modelList[2].predict(userFeatures)
        neu = modelList[3].predict(userFeatures)
        ext = modelList[4].predict(userFeatures)
        agr = modelList[5].predict(userFeatures)
        con = modelList[6].predict(userFeatures)
    valueList.append(age)
    valueList.append(gender)
    valueList.append(ope)
    valueList.append(neu)
    valueList.append(ext)
    valueList.append(agr)
    valueList.append(con)
	#valueList.extend(age, gender, ope, neu, ext, agr, con)
    return valueList
		                
        
#getImages()
#clfRFgender, clfRFage, linRegOpe = getImages()
#predictPublicData(clfRFgender, clfRFage, linRegOpe)
#predictPublicData(clfRFgender, clfRFage)

