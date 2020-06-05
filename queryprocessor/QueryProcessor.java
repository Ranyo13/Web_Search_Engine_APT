package apt_project;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class QueryProcessor {
	public static HashSet<String> hs = new HashSet<String>();
	//public static Ranker r;

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
	
    /**
    * @param actualQuery is the actual string the user entered to search for
    * @param ifPhrase indicated if the user is searching for the exact phrase or not (1 if yes)
    * @param queryProcessed is the string of words the user wants to search for after being processed by the query processor 
    * @param userCountry is the user's country
    * @param ifWebs indicates whether the user is looking for results of websites or images (1 is for websites)
    */
	
	public static void main(String[] args) {
		//ALL OF THE FOLLOWING ARGUMENTS SHOULD COME FROM THE WEB-INTERFACE (HTTP REQUEST)//
		////////////////////////////////////////////////////////////////////////////////////
		String actualQuery = "will CaPiTaLs chaNge ?";
		boolean ifPhrase = false;
		actualQuery = actualQuery.toLowerCase();
		if(actualQuery.startsWith("\"") && actualQuery.endsWith("\""))
			ifPhrase = true;
		boolean ifWebs = true;
		String userCountry = "Egypt";
		/////////////////////////////////////////////////////////////////////
		String processedQuery = tokenizeStopStem(actualQuery);
		String[] keyWords = processedQuery.split(" ");
//        Database db = new Database();
//        Connection conn = db.openConnection();
		Connection conn;
		conn = null;
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "apt_proj";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = "";
		try {
		Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName,userName,password);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Connected to the database");
        
        List<webResult> results = new ArrayList<>();
        List<imgResult> imgresults = new ArrayList<>();

        try {
	        if(ifWebs) {
	            Ranker r = new Ranker();
	            r.rank(conn,keyWords,results,userCountry);
	            
	            if(ifPhrase) {
	                phraseSearch p = new phraseSearch();
	                p.RankWebs(actualQuery,results);
	            }
	        } else {
	            imageRanker i = new imageRanker();
	            i.rank(conn,keyWords,imgresults);
	        }
        }
        catch(Exception e)
        {
        	
        }
        
        //RETURN REQUIRED BY INTERFACE IN A HTTP RESPONSE HERE
	}

}
