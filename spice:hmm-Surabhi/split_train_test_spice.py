#!/usr/bin/python

DATA = "data/ALLCAUSE_COST_SPICE.txt"
TRAIN = "train_spice_allcost.txt"
TEST = "test_spice_allcost.txt"

def sep_data(fr,fwtr,fwte,num_train,num_test,num_classes):
	fwtr.write(str(num_train))
	fwtr.write(" " + num_classes + "\n")

	fwte.write(str(num_test))
	fwte.write(" " + num_classes + "\n")

	count = 0
	for line in fr:
		count += 1  
		row = line.rstrip().split(" ")
		
		if count <= num_train:
			fwtr.write(" ".join(row) + "\n")
		else:
			fwte.write(" ".join(row) + "\n")


def main():
	fr = open(DATA,"r")
	header = fr.readline().rstrip().split(" ")
	num_rows = int(header[0])
	num_classes = header[1]

	num_train = round(80.0/100.0 * num_rows)
	num_test =  round(20.0/100.0 * num_rows)

	fwtr = open(TRAIN,"w")
	fwte = open(TEST,"w")

	sep_data(fr,fwtr,fwte,num_train,num_test,num_classes)

	fwtr.close()
	fwte.close()
	fr.close()

if __name__ == '__main__':
    main()