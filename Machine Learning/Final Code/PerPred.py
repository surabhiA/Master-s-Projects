import csv

import numpy as np
import pandas as pd
from sklearn import cross_validation
from sklearn import tree, linear_model

####################################################################
def createDataFrame() :
    datafile = '/data/training/profile/profile.csv'
    df1 = pd.read_csv(datafile,sep=',')
    df2 = pd.read_csv('/data/training/LIWC.csv', sep = ',')
    df_merge = pd.merge(left = df2,right = df1, left_on = 'userId', right_on = 'userid')
    df_merge.drop(['Unnamed: 0', 'userid', 'age', 'gender', 'userId'],inplace=True,axis=1,errors='ignore')
    yOpe = df_merge[['ope']]
    yExt = df_merge[['ext']]
    yCon = df_merge[['con']]
    yAgr = df_merge[['agr']]
    yNeu = df_merge[['neu']]
    df_merge.drop(['ope','ext','con','agr','neu'],inplace=True,axis=1,errors='ignore')
    return df_merge, yOpe,yExt, yCon, yAgr, yNeu

def createTestLIWCDf(testpath):
    df = pd.read_csv(testpath + '/LIWC.csv', sep = ",")
    return df

def getuserLIWC(df, userid):
    return df.loc[df['userId'] == userid]


def trainEachPeronality(X, Y) :
    regr = linear_model.LinearRegression()
    regr.fit(X, Y)
    return regr

def rmse(model, Xtest, Ytest) :
    print(np.mean((model.predict(Xtest) - Ytest) ** 2))


def testModel() :
    X, YOPE, YExt, YCon, YAgr, YNeu = createDataFrame()
    XOpe_train, XOpe_test, yOpe_train, yOpe_test = cross_validation.train_test_split(X, YOPE, test_size=0.15, random_state=0)
    regrOpe = trainEachPeronality(XOpe_train, yOpe_train, 'ope')
    rmse(regrOpe, XOpe_test, yOpe_test)

    XExt_train, XExt_test, yExt_train, yExt_test = cross_validation.train_test_split(X, YExt, test_size=0.15, random_state=0)
    regrExt = trainEachPeronality(XExt_train, yExt_train, 'ext')
    rmse(regrExt, XExt_test, yExt_test)

    XCon_train, XCon_test, yCon_train, yCon_test = cross_validation.train_test_split(X, YCon, test_size=0.15, random_state=0)
    regrCon = trainEachPeronality(XCon_train, yCon_train, 'con')
    rmse(regrCon, XCon_test, yCon_test)


    XNeu_train, XNeu_test, yNeu_train, yNeu_test = cross_validation.train_test_split(X, YNeu, test_size=0.15, random_state=0)
    regrNeu = trainEachPeronality(XNeu_train, yNeu_train, 'neu')
    rmse(regrNeu, XNeu_test, yNeu_test)


    XAgr_train, XAgr_test, yAgr_train, yAgr_test = cross_validation.train_test_split(X, YAgr, test_size=0.15, random_state=0)
    regrAgr = trainEachPeronality(XAgr_train, yAgr_train, 'agr')
    rmse(regrAgr, XAgr_test, yAgr_test)
