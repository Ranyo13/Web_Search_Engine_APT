package apt_project;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.sql.*;

import opennlp.tools.namefind.NameFinderME; 
import opennlp.tools.namefind.TokenNameFinderModel; 
import opennlp.tools.util.Span;

public class Trends {
	
	public static void addTrend (String query, String country, Connection conn)
	{
		
		try {
			//FETCH PERSON ML MODEL
			InputStream is = new FileInputStream("en-ner-person.bin");
	        TokenNameFinderModel model = new TokenNameFinderModel(is);
	        is.close();
	        //FEED THE NAMEFINDER CLASS THE MODEL
	        NameFinderME nameFinder = new NameFinderME(model);
	        //SPLIT THE QUERY FOR THE NAMEFINDER CLASS TO BE ABLE TO OPERATE ON IT
	       	String[] splitQuery = query.split("\\s");
	       	//RESULTANT STRING FROM NLP
	       	String result = new String();
	        Span nameSpans[] = nameFinder.find(splitQuery);
	       	for(Span span: nameSpans){
	       		result = "";
	        	for(int index=span.getStart();index<span.getEnd();index++){
	        		if(index == span.getEnd() - 1)
	        			result = result + splitQuery[index];
	        		else
	        			result = result + splitQuery[index] + " ";
	            }
	        	//IF THERE IS RESULT, INSERT IT IF THIS NAME IS NEW TO COUNTRY
	        	//IF THE COUNTRY ALREADY HAS THIS NAME INCREASE COUNT BY 1
	        	if(!result.isBlank())
	        	{
	        		
	        		String insertionOrUpdate=" INSERT INTO trends SET country = ?, name = ?, count = 1  ON DUPLICATE KEY UPDATE count = count + 1";
	        		PreparedStatement insertOrUpdateTrend = conn.prepareStatement(insertionOrUpdate);
	        		insertOrUpdateTrend.setString(1, country); 
	        		insertOrUpdateTrend.setString(2, result); 
	        		insertOrUpdateTrend.executeUpdate();

	        	}
	        }
	        
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
       
	}
	
///// THE FOLLOWING COMMENTED CODE IS FOR VIDEO DELIVERY AND PERFORMANCE ANALYSIS /////
	
//	public static void main(String[] args) throws Exception {
//		Connection conn;
//		conn = null;
//		String url = "jdbc:mysql://localhost:3306/";
//		String dbName = "apt_proj";
//		String driver = "com.mysql.jdbc.Driver";
//		String userName = "root";
//		String password = "";
//		try {
//		Class.forName(driver).newInstance();
//		conn = DriverManager.getConnection(url+dbName,userName,password);
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		
//		System.out.println("Connected to the database");
//		long start = System.currentTimeMillis();
//		Trends.addTrend("Ryan Reynolds", "Egypt", conn);
//		long finalTime = System.currentTimeMillis();
//		long duration = (finalTime-start);
//		System.out.println("Added trend from given query in " + duration + " milliseconds.");
//	}
}
