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
	public static void main(String[] args) throws Exception {

		SparseMatrixBuilderWW b = new SparseMatrixBuilderWW();

		b.tokenDict = b.parseDict("tokenDictionary.txt", 10000);
//		System.out.println(b.tokenDict.size());
		b.tagDict = b.parseDict("tagDictionary.txt", -1);
//		System.out.println(b.tagDict.size());
		b.buildRows("QueryResults1800000.csv");
	}

	private HashMap<String, Integer> parseDict(String filename, int threshold)
			throws Exception {
		HashMap<String, Integer> dict = new HashMap<String, Integer>();

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		int numLines = 0;
		while ((line = reader.readLine()) != null) {
			if(threshold != -1 && numLines >= threshold) {
				break;
			}
			String[] components = line.split("\t");
			dict.put(components[0], numLines);
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
		while ((entry = listReader.read(processors)) != null) {
			// sanity check the CSV parsing
			if (entry.size() != 14) {
				continue;
			}
			
			// hi Derrick, do stuff with the question body here!
			String questionBody = (String) entry.get(9);
			
			// compile list of token indices
			HashMap<String, Integer> wordCounts = countWords(questionBody);
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
			int numCodeExamples = getNumInstances(Pattern.compile("<pre><code>"), questionBody);
						
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			int numEdits = Integer.parseInt((String)entry.get(13));
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
			
			String row = "" + label;
			for(int[] coord : tokenIndices) {
				row += " " + coord[0] + " " + coord[1]; 
			}
			for(int[] coord : tagIndices) {
				row += " " + (coord[0] + tokenFeaturesOffset) + " " + coord[1]; 
			}
			row += " " + tagFeaturesOffset + " " + numEdits;
			row += " " + (tagFeaturesOffset + 1) + " " + editTimeElapsed;
			bw.write(row.toCharArray());
			bw.write('\n');
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
	
	private int getNumInstances(Pattern pattern, String str) {
		int numMatches = 0;
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()) {
			numMatches++;
		}
		return numMatches;
	}
	
	private CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] { null, null,
				null, null, null, null, null, null, null, null, null, null,
				null, null };

		return processors;
	}

}
