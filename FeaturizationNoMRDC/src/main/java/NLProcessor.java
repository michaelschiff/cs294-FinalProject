import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;

//Brown Parser: http://www.cs.cmu.edu/~radar/dmg/MCALL/lingpipe-3.6.0/docs/api/com/aliasi/corpus/parsers/BrownPosParser.html
//http://www.cs.cmu.edu/~radar/dmg/MCALL/lingpipe-3.6.0/demos/tutorial/posTags/src/RunMedPost.java
//http://www.cs.cmu.edu/~radar/dmg/MCALL/lingpipe-3.6.0/demos/tutorial/posTags/read-me.html
// Collocations: http://www.cs.cmu.edu/~radar/dmg/MCALL/lingpipe-3.6.0/demos/tutorial/interestingPhrases/read-me.html

public class NLProcessor {
	HmmDecoder decoder;
	TokenizerFactory TOKENIZER_FACTORY;

	public NLProcessor() {
		TOKENIZER_FACTORY = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");
		try {
			FileInputStream fileIn = new FileInputStream(
					"../pos-en-general-brown.HiddenMarkovModel");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
			Streams.closeInputStream(objIn);
			decoder = new HmmDecoder(hmm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// http://www.cs.cmu.edu/~radar/dmg/MCALL/lingpipe-3.6.0/docs/api/com/aliasi/corpus/parsers/BrownPosParser.html
	public HashMap<String,Integer> getTagCounts(String text)
	{
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),
				0, text.toCharArray().length);
		String[] tokens = tokenizer.tokenize();
		String[] tags = decoder.firstBest(tokens);
		for (int i = 0; i < tokens.length; ++i) {
			if (counts.containsKey(tags[i]))
			{
				counts.put(tags[i], 1);
			}
			else 
			{
				counts.put(tags[i], counts.get(tags[i])+1);
			}
		}
		return counts;
	}
	
	private String tag(String text) {
		String tagInfo = "";
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),
				0, text.toCharArray().length);
		String[] tokens = tokenizer.tokenize();
		String[] tags = decoder.firstBest(tokens);
		for (int i = 0; i < tokens.length; ++i) {
			tagInfo += (tokens[i] + "_" + tags[i] + " ");
		}
		return tagInfo;
	}

}
