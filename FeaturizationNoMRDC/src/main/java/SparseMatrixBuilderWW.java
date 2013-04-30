import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * 0: Id 1: Label 2: Title 3: Score 4: Tags 5: CreationDate 6: ViewCount 7:
 * LastEditDate 8: LastActivityDate 9: Body 10: AnswerCount 11: CommentCount 12:
 * FavoriteCount 13: NumEdits
 */

public class SparseMatrixBuilderWW {
	HashMap<String, Integer> tokenDict;
	HashMap<String, Integer> tagDict;
	HashMap<String, Integer> nlDict; //Dictionary for telling which tag goes where
	NLProcessor nlProcessor;
	int numEntriesThreshold = Integer.MAX_VALUE; // Maximum number of entries to go through
	
	public SparseMatrixBuilderWW()
	{
		nlProcessor = new NLProcessor(); // Added for Processing
	}

	private HashMap<String, Integer> parseDict(String filename, int maxDictSize, int freqThreshold)
			throws Exception {
		HashMap<String, Integer> dict = new HashMap<String, Integer>();

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		int numLines = 1;
		while ((line = reader.readLine()) != null) {
			if(maxDictSize != -1 && numLines >= maxDictSize) {
				break;
			}
			String[] components = line.split("\t");
			int freq = Integer.parseInt(components[1]);
			if (freq>freqThreshold)
			{
				dict.put(components[0], numLines);
			}
			numLines++;
		}
		return dict;
	}

	private void buildRows(String dataFilename) throws Exception {
		File file = new File("matrix.txt");
		if(!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		CsvListReader listReader = new CsvListReader(new FileReader(dataFilename),
				CsvPreference.STANDARD_PREFERENCE);
		listReader.getHeader(true); // skip the header (can't be used with
									// CsvListReader)
		final CellProcessor[] processors = getProcessors();
		List<Object> entry;
		int entryCt = 1;
		while (entryCt < numEntriesThreshold && (entry = listReader.read(processors)) != null) {
			
			// sanity check the CSV parsing
			if (entry.size() != 14) {
				continue;
			}
			
			// hi Derrick, do stuff with the question body here!
			String fullQuestionBody = (String) entry.get(9);
			int numFullBodyWords = fullQuestionBody.split("\\s+").length;
			String[] codeNonCode = splitCodeNonCode(fullQuestionBody);
			String codeBody = codeNonCode[0];
			String questionBody = codeNonCode[1];
			int numQuestionBodyWords = questionBody.split("\\s+").length;
			int numSentencesForQuestionBody = getNumInstances(Pattern.compile("([\\.?!][\\s+<])|(\\s+</p>)"), questionBody);
			
			int numCodeBodyWords = codeBody.equals("") ? 0 : codeBody.split("\\s+").length;

			//TODO: strip questionBody of all the code and put that into codeBody variable. 
			//TODO: add feature for code length
//			HashMap<Integer,Integer> nlFeatureCounts = new HashMap<Integer,Integer>(); 
			HashMap<Integer,Integer> nlFeatureCounts = getNLFeatures(questionBody);
			
			// compile list of token indices
			HashMap<String, Integer> wordCounts = countWords(fullQuestionBody);
			List<int[]> tokenIndices = new LinkedList<int[]>();
			for(Entry<String, Integer> wcPair : wordCounts.entrySet()) {
				if(this.tokenDict.containsKey(wcPair.getKey())) {
					int[] coord = {this.tokenDict.get(wcPair.getKey()), wcPair.getValue()}; 
					tokenIndices.add(coord);
				}
			}
			
			// compile list of tag indices
			String tagBody = (String) entry.get(4);
			String[] tags = tagBody.split(">");
			List<int[]> tagIndices = new LinkedList<int[]>();
			for(String tag : tags) {
				tag = tag.substring(1);
				if(this.tagDict.containsKey(tag)) {
					int[] coord = {this.tagDict.get(tag), 1};
					tagIndices.add(coord);
				}
			}
			
			// get misc features
			int numCodeExamples = getNumInstances(Pattern.compile("<pre><code>"), codeBody);
						
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			int numEdits=0;
			try {
				numEdits = Integer.parseInt(((String)entry.get(13)).trim());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("something is wrong.");
			}
			String creationDate = (String) entry.get(5);
			String editDate = (String) entry.get(7);
//			System.out.println(entry);
			long editTimeElapsed = 0;
			if(editDate != null && creationDate != null) {
				editTimeElapsed = dateFormat.parse(editDate).getTime() - dateFormat.parse(creationDate).getTime();
			}
			int label = Integer.parseInt((String) entry.get(1));
			
			int tokenFeaturesOffset = tokenDict.size();
			int tagFeaturesOffset = tokenFeaturesOffset + tagDict.size();
			int nlFeaturesOffset = tagFeaturesOffset + nlDict.size();
			
			String row = "" + label;
			// prepare to write out token features
			for(int[] coord : tokenIndices) {
				row += (" " + coord[0] + " " + coord[1]); 
			}
			
			// prepare to  write out tag features
			for(int[] coord : tagIndices) {
				row += (" " + (coord[0] + tokenFeaturesOffset) + " " + coord[1]); 
			}
			
			// TODO: prepare write out nl features
			for (Entry<Integer,Integer> ent  : nlFeatureCounts.entrySet())
			{
				row += (" " + (ent.getKey() + tagFeaturesOffset)+ " " + ent.getValue());
			}
			
			// prepare to write out misc features TODO: add total number of words and number of paragraphs (aka. number of newlines)
			row += " " + (nlFeaturesOffset + 1) + " " + numEdits;
			row += " " + (nlFeaturesOffset + 2) + " " + editTimeElapsed;
			row += " " + (nlFeaturesOffset + 3) + " " + numCodeExamples;
			row += " " + (nlFeaturesOffset + 4) + " " + numQuestionBodyWords;
			row += " " + (nlFeaturesOffset + 5) + " " + numSentencesForQuestionBody;
			if (numCodeBodyWords > 0)
			{
			row += " " + (nlFeaturesOffset + 6) + " " + numCodeBodyWords;
			}
			
			//THINGS TO PRINT
			if (entryCt%1000==0)
			{
				System.out.println("Entry #:"+entryCt);
			}
//			System.out.println("# of Active Features: " + row.split("\\s+").length/2);
//			System.out.println("# of words for full body: " + numFullBodyWords);
//			System.out.println("# of sentences for question body: " + numSentencesForQuestionBody);
//			System.out.println("# of words for question body: " + numQuestionBodyWords);
//			System.out.println("# of words for code body: " + numCodeBodyWords);
			
			// Write all the features
			bw.write(row.toCharArray());
			bw.write('\n');
			entryCt+=1;
		}

		if (listReader != null) {
			listReader.close();
		}
		
		bw.close();

	}
	
	private HashMap<String, Integer> countWords(String text) {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		String[] splitText = text.split("\\s+");
		for(String word : splitText) {
			if(counts.containsKey(word)) {
				counts.put(word, counts.get(word) + 1);
			} else {
				counts.put(word, 1);
			}
		}
		return counts;
	}
	
	//</code></pre>
//	private String removeAll(String pattern, String str, String newStr)
//	{
//		return str.replaceAll(pattern,newStr);
//	}
//	
//	private List<String> getMatches(Pattern pattern, String str)
//	{
//		List<String> matchStrs = new LinkedList<String>();
//		Matcher matcher = pattern.matcher(str);
//		while (matcher.find())
//		{
//			matchStrs.add(matcher.group());
//		}
//		return matchStrs;
//	}
//	
//	private int getLineCtOfAllCode(List<String>strMatches)
//	{
//		int lineCt = 0;
//		Pattern newLinePattern = Pattern.compile("\n");
//		for (String str : strMatches)
//		{
//			lineCt+=getNumInstances(newLinePattern,str);
//		}
//		return lineCt;
//	}
	
	private String[] splitCodeNonCode(String str)
	{
		String code="",nonCode ="";
		Pattern startCode = Pattern.compile("<pre><code>");
		Pattern endCode = Pattern.compile("</code></pre>");
		String[] lines = str.split("\\n");
		int ct = 0;
		String line;
		Boolean isCode = false;
		while (ct < lines.length)
		{
			line = lines[ct]+ " "; //added make things easier when counting number of sentences
			if (endCode.matcher(line).find())
			{
				isCode = false;
				code+=line;
				ct++;
				continue;
			} 
			if (startCode.matcher(line).find())
			{
				isCode = true;
				code+=line;
				ct++;
				continue;
			}
			if (isCode)
			{
				code+=line;
			} else
			{
				nonCode+=line;
			}
			ct++;
		}
		String[] toReturn = new String[]{code,nonCode};
		return toReturn;
	}
	
	private int getNumInstances(Pattern pattern, String str) {
		int numMatches = 0;
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()) {
			numMatches++;
		}
		return numMatches;
	}
	
	private HashMap<Integer,Integer> getNLFeatures(String text)
	{
		// Counts occurrences of each NL Tag as well as sentences length
		return nlProcessor.getNLCounts(text,nlDict);
	}
	
	private CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] { null, null,
				null, null, null, null, null, null, null, null, null, null,
				null, null };

		return processors;
	}

	public static void main(String[] args) throws Exception {

		SparseMatrixBuilderWW b = new SparseMatrixBuilderWW();

		b.tokenDict = b.parseDict("data/sortedTokenDict.txt", -1, 15);//10000
//		System.out.println(b.tokenDict.size());
		b.tagDict = b.parseDict("data/sortedTagDict.txt", -1,-1);
//		System.out.println(b.tagDict.size());
		b.nlDict = b.parseDict("data/nlDict.txt", -1,-1);
//		System.out.println(b.nlDict.size());
		b.buildRows("data/megaResults.csv");
	}
}
