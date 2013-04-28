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

public class SparseMatrixBuilderDC {
	HashMap<String, Integer> tokenDict;
	HashMap<String, Integer> tagDict;
	HashMap<String, Integer> nlDict; //Dictionary for telling which tag goes where
	NLProcessor nlProcessor;
	
	public SparseMatrixBuilderDC()
	{
		nlProcessor = new NLProcessor(); // Added for Processing
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
			getNLFeatures(questionBody);
			
			String row = "";
//			row+=rowPart;
			bw.write(row.toCharArray());
			bw.write('\n');
		}

		if (listReader != null) {
			listReader.close();
		}
		
		bw.close();

	}
	
	private HashMap<String,Integer> getNLFeatures(String text)
	{
		// Counts occurrences of each NL Tag as well as sentences length
		return nlProcessor.getNLCounts(text);
	}
	
	private CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] { null, null,
				null, null, null, null, null, null, null, null, null, null,
				null, null };

		return processors;
	}

	
	public static void main(String[] args) {

		System.out.println();
		SparseMatrixBuilderDC b = new SparseMatrixBuilderDC();
		try {
			b.buildRows("data/QueryResults500000.csv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		b.tokenDict = b.parseDict("tokenDictionary.txt", 10000);
//		System.out.println(b.tokenDict.size());
//		b.tagDict = b.parseDict("tagDictionary.txt", -1);
//		System.out.println(b.tagDict.size());
	}
}
