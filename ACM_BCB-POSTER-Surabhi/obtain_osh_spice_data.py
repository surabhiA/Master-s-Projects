import sys, os
import numpy

FILE = 'osh_spice_data/private/OSHPD_CHF_LOS_SPICE.txt'
#FILE = '/Users/surabhi/Documents/CapstoneDocs/spice_proj/sample_data/train_spice.txt'

def main():
	fr = open(FILE, "r")
	data_info = fr.readline().rstrip().split(" ")
	num_seq = int(data_info[0])
	print "Num Seq:", num_seq

	min_seq_len = num_seq
	max_seq_len = 0
	len_sum = 0
	sequence_lengths = []

	for line in fr:
		row = line.rstrip().split(" ")
		seq_len = int(row[0])
		if(seq_len < min_seq_len):
			min_seq_len = seq_len

		if(seq_len > max_seq_len):
			max_seq_len = seq_len

		len_sum += seq_len
		sequence_lengths.append(seq_len)

	arr = numpy.array(sequence_lengths)


	print "Max seq len:", max_seq_len
	print "Min seq len:", min_seq_len
	print "Avg seq len:", float(len_sum)/num_seq
	print "Std deviation population:", numpy.std(arr, ddof=0)
	print "Std deviation samples:", numpy.std(arr, ddof=1)


if __name__ == '__main__':
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)
    main()