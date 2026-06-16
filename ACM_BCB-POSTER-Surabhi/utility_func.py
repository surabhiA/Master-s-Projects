#!/usr/bin/python
import operator

FILE = 'data/four_or_more/age_less_65/private/CHF_LOS_PRIVATE_SPICE.csv'

SAVE_AS = 'data/four_or_more/age_less_65/ConvertedFormat/private/CHF_LOS_PRIVATE_SPICE.csv'


#Writes 10% of data to another file
def tenper(FILE, SAVE_AS):

    fr = open(FILE,"r")
    fw = open(SAVE_AS,"w")

    header = fr.readline().rstrip().split(" ")
    num_lines = int(header[0])
    num_symbols = header[1]

    target_lines = int((20.0 / 100) * num_lines)
    print "target lines:", target_lines

    fw.write(str(target_lines) + " " + num_symbols + "\n")

    count = 0
    for line in fr:
        row = line.rstrip().split(" ")
        count += 1
        fw.write(" ".join(row) + "\n")
        if(count == target_lines):
            break

    fr.close()
    fw.close()


#Converts seq 123-->012
def convertFormat(FILE, SAVE_AS):
    fr = open(FILE,"r")
    fw = open(SAVE_AS,"w")

    header = fr.readline().rstrip().split(" ")
    num_lines = header[0]
    num_symbols = int(header[1]) - 1

    fw.write(num_lines + " " + str(num_symbols) + "\n")

    for line in fr:
        row = line.rstrip().split(" ")
        seqlen = row[0]
        seq = row[1:]

        newSeq = []
        for value in seq:
            newSeq.append(str(int(value) - 1))

        fw.write(seqlen + " ")
        fw.write(" ".join(newSeq))
        fw.write("\n")

    fr.close()
    fw.close()


#Writes sequences of len 4 or more to another file and returns the maj label in the new file
def four_or_more(FILE, SAVE_AS):

    fr = open(FILE,"r")
    fw = open(SAVE_AS,"w")

    maj_label = {}

    fr.readline()

    count = 0
    for line in fr:
        row = line.rstrip().split(" ")
        label = row[len(row) - 1]

        if(int(row[0]) < 4):
            continue

        count += 1
        if(label in maj_label):
            maj_label[label] += 1
        else:
            maj_label[label] = 1

    #print maj_label
    sorted_prob = sorted(maj_label.items(), key=operator.itemgetter(1))
    #print sorted_prob


    fr.seek(0)

    header = fr.readline().rstrip().split(" ")
    header[0] = str(count)
    fw.write(" ".join(header) + "\n")

    for line in fr:
        row = line.rstrip().split(" ")
        if(int(row[0]) < 4):
            continue

        fw.write(" ".join(row) + "\n")

    fr.close()
    fw.close()

    #returns majority label in new file
    return sorted_prob[len(sorted_prob) - 1][0]


def main():
    convertFormat(FILE, SAVE_AS)

####################################################### 

if __name__ == '__main__':
    main()