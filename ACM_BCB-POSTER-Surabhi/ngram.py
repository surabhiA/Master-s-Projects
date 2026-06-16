import ocsv, sys, random

def predict(prefix, ngrams, syms):
  #List of possible symbols to be tested (Includes -1)
  allsyms = [-1] + list(range(syms))
  #Create a dictionary of symbol probabilities
  score = {sym:dict() for sym in allsyms}

  #Append -1 to test sequence
  prefix = tuple([-1] + prefix)
  
  #Get number of ngrams from commandline, else use default ngram = 3
  n = int(sys.argv[3]) if len(sys.argv) > 3 else 3

  #Get the number of grams to be computed for that seq based on its length.
  grams = range(min(len(prefix) + 1, n), min(len(prefix) + 2, n + 1))
  
  for gram in grams:
    #total number of times the last tuple occurs (Without appending last symbol to prefix)
    total = sum([ngrams[gram][tp] for tp in ngrams[gram] if tp[:gram-1] == prefix[1-gram:]])

    for sym in allsyms:
      #append symbol to end of n-1 gram tuple to from an ngram tuple
      gramseq = prefix[1 - gram:] + (sym,)
      if gramseq in ngrams[gram]:
        # score each gram by the probability
        score[sym][gram] = ngrams[gram][gramseq] / float(total)

  for sym in allsyms:
    scoredict = score[sym]
    totalgram = sum(scoredict.keys())
    # probability weighted by gram
    scoresym = [item[0]*item[1]/float(totalgram) for item in scoredict.items()]
    score[sym] = scoresym
  return [item[0] for item in sorted(list(score.items()), reverse = True, key = lambda pair: pair[1])]


def train(rows):
  global ngrams
  ngrams = [[]]
  #Get number of ngrams from commandline, else use default ngram = 3
  n = int(sys.argv[3]) if len(sys.argv) > 3 else 3
  # Get frequencies for all n grams
  for gram in range(1, n + 1):
    igram = dict()
    for row in rows:
      #append -1 infront of each sequence to indicate beginning of a seq.
      row = [-1] + row

      #get freq of all tuples of gram n
      for j in range(len(row) - gram + 1):
        key = tuple(row[j:j+gram])
        igram[key] = igram[key] + 1 if key in igram else 1
    ngrams.append(igram)
  
  return ngrams

def test(syms, rows):
  #remove sequences of length 0
  rows = [row for row in rows if len(row) > 0]

  #Get number of sequences
  lines = len(rows)

  #Get test file path from commanline
  if len(sys.argv) > 2:
    testpath = sys.argv[2]

  #Get Number of sequences, number of possible symbols and sequences in the form of array- from test file
  lines, syms, testrows = readData(testpath)
  correct = 0

  #obtain trained model
  ngrams = train(rows)
  
  for testrow in testrows:
    #Get prediction for each test sequence and calculate accuracy
    results = predict(testrow[:-1], ngrams, syms)
    correct += (1 if results[0] == testrow[-1] else 0)
  print('accuracy ', correct / float(lines))

def readData(path):
  syms = 0
  rows = []
  with open(path, 'r') as fin:
    #Get number of sequences and number of possible symbols
    lines, syms = [int(item) for item in fin.readline().strip().split(' ')]

    #Get all the sequences in the form of arrays within an array
    ocsv.runFunc(fin, lambda line:rows.append([int(item) for item in line.strip().split(' ')[1:]]))
    
  return lines, syms, rows

def main():
  global trainpath
  if len(sys.argv) > 1:
    #Get train path from commandline
    trainpath = sys.argv[1]
  lines, syms, rows = readData(trainpath)
  test(syms, rows)

if __name__ == "__main__":
    main()