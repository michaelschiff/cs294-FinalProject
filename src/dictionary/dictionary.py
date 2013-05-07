import csv
import sys
import fnmatch
import os

usage = """Usage:\n\tpython dictionary.py tag/token directory"""
STOP_WORDS = ['a','able','about','across','after','all','almost','also','am','among','an','and','any','are','as','at','be','because','been','but','by','can','cannot','could','dear','did','do','does','either','else','ever','every','for','from','get','got','had','has','have','he','her','hers','him','his','how','however','i','if','in','into','is','it','its','just','least','let','like','likely','may','me','might','most','must','my','neither','no','nor','not','of','off','often','on','only','or','other','our','own','rather','said','say','says','she','should','since','so','some','than','that','the','their','them','then','there','these','they','this','tis','to','too','twas','us','wants','was','we','were','what','when','where','which','while','who','whom','why','will','with','would','yet','you','your']
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
                            in_code = False
                            y = x.split(delimeter) if delimeter else x.split()
                            for t in y:
                                if '<code>' in t:
                                    in_code = True
                                elif '</code>' in t:
                                    in_code = False
                                if t != "" and not in_code and t not in STOP_WORDS:
                                    tf = self.reformat(t)
                                    counts[tf] = counts.get(tf, 0) + 1
        sorted_counts = counts.items()
        sorted_counts.sort(key=lambda item: item[1], reverse=True)
        for k,v in sorted_counts:
            print k + '\t' + str(v)

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
