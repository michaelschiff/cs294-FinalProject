nlTags = open('nlTags.txt','rb')
nlDict = open('nlDict.txt','wb')
ct = 1
for tag in nlTags:
	nlDict.write(tag.strip().lower() + '\t' + str(ct)+'\n')
	ct = ct + 1
nlTags.close()
nlTags.close()
