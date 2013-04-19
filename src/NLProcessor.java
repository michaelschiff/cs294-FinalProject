import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;


public class NLProcessor {
	
	static TokenizerFactory TOKENIZER_FACTORY 
    	= new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");

	public static void main(String[] args) throws Exception{
		FileInputStream fileIn = new FileInputStream("pos-en-general-brown.HiddenMarkovModel");
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
        Streams.closeQuietly(objIn);
        HmmDecoder decoder = new HmmDecoder(hmm);
        
        String foo = "this is a test.";
        
        Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(foo.toCharArray(), 0, foo.toCharArray().length);
        String[] tokens = tokenizer.tokenize();
        List<String> tokenList = Arrays.asList(tokens);

        firstBest(tokenList,decoder);


	}
	
	static void firstBest(List<String> tokenList, HmmDecoder decoder) {
        Tagging<String> tagging = decoder.tag(tokenList);
        System.out.println("\nFIRST BEST");
        for (int i = 0; i < tagging.size(); ++i)
            System.out.print(tagging.token(i) + "_" + tagging.tag(i) + " ");
        System.out.println();

    }
}
