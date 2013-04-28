import java.io.FileInputStream;
import java.io.ObjectInputStream;

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

public class NLProcessorStatic {
	
	
	static TokenizerFactory TOKENIZER_FACTORY 
    	= new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");

	public static void main(String[] args) throws Exception{
		FileInputStream fileIn = new FileInputStream("../pos-en-general-brown.HiddenMarkovModel");
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
        Streams.closeInputStream(objIn);
        HmmDecoder decoder = new HmmDecoder(hmm);
        
        String foo = "this is a test.";
        
        Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(foo.toCharArray(), 0, foo.toCharArray().length);
        String[] tokens = tokenizer.tokenize();

        firstBest(tokens,decoder);

	}
	
	static void firstBest(String[] tokens, HmmDecoder decoder) {
        String[] tags = decoder.firstBest(tokens);
//        System.out.println("\nFIRST BEST");
        for (int i = 0; i < tokens.length; ++i)
            System.out.print(tokens[i] + "_" + tags[i] + " ");
        System.out.println();

    }

}
