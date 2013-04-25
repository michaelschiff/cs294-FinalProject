import csv
import sys
import fnmatch
import os

usage = """Usage:\n\tpython dictionary.py tag/token directory"""

class base:
    def reformat(self, word):
        return word
    def __init__(self, directory, delimeter, recIndex):
        counts = {}
        pattern = '*.csv'
        for root, dirs, files in os.walk(directory):
            for filename in fnmatch.filter(files, pattern):
                recordFilePath = os.path.join(root, filename)
                with open(recordFilePath, 'rb') as csvFile:
                    recordReader = csv.reader(csvFile)
                    for record in recordReader:
                        for x in record[recIndex].split('\n'):
                            y = x.split(delimeter) if delimeter else x.split()
                            for t in y:
                                if t != "":
                                    tf = self.reformat(t)
                                    counts[tf] = counts.get(tf, 0) + 1
        for key in counts:
            print key + '\t' + str(counts[key])

class tagDictionary(base):
    def reformat(self, word):
        return word[1:].lower()
    def __init__(self, directory):
        base.__init__(self, directory, '>', 4)

class tokenDictionary(base):
    def reformat(self, word):
        return word.lower()
    def __init__(self, directory):
        base.__init__(self, directory, None, 9)

    


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print usage
    elif sys.argv[1] == "tag":
        tagDictionary(sys.argv[2])
    elif sys.argv[1] == "token":
        tokenDictionary(sys.argv[2])
    else:
        print usage
