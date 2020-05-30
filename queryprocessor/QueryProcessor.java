package apt_project;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;


public class QueryProcessor {

	public static HashSet<String> hs = new HashSet<String>();


	public static String[] stopwords = {"a", "able", "about",
	        "across", "after", "all", "almost", "also", "am", "among", "an",
	        "and", "any", "are", "as", "at", "b", "be", "because", "been",
	        "but", "by", "c", "can", "cannot", "could", "d", "dear", "did",
	        "do", "does", "e", "either", "else", "ever", "every", "f", "for",
	        "from", "g", "get", "got", "h", "had", "has", "have", "he", "her",
	        "hers", "him", "his", "how", "however", "i", "if", "in", "into",
	        "is", "it", "its", "j", "just", "k", "l", "least", "let", "like",
	        "likely", "m", "may", "me", "might", "most", "must", "my",
	        "neither", "n", "no", "nor", "not", "o", "of", "off", "often",
	        "on", "only", "or", "other", "our", "own", "p", "q", "r", "rather",
	        "s", "said", "say", "says", "she", "should", "since", "so", "some",
	        "t", "than", "that", "the", "their", "them", "then", "there",
	        "these", "they", "this", "tis", "to", "too", "twas", "u", "us",
	        "v", "w", "wants", "was", "we", "were", "what", "when", "where",
	        "which", "while", "who", "whom", "why", "will", "with", "would",
	        "x", "y", "yet", "you", "your", "z"};
	public static void StopWords()
	{
	    int len= stopwords.length;
	    for(int i=0;i<len;i++)
	    {
	        hs.add(stopwords[i]);
	    }
	}
	
	private static String tokenizeStopStem(String input) {
		StopWords();
        TokenStream tokenStream = new StandardTokenizer(
                Version.LUCENE_36, new StringReader(input));
        tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, hs);
        tokenStream = new PorterStemFilter(tokenStream);
 
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttr = tokenStream.getAttribute(CharTermAttribute.class);
        try{
            while (tokenStream.incrementToken()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(charTermAttr.toString());
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return sb.toString();
	}
	
	public static String processQuery(String str)
	{
		return tokenizeStopStem(str);
	}
	
	public static void main(String[] args) {
		System.out.println(QueryProcessor.processQuery("when was the last match played?"));
	}

}
