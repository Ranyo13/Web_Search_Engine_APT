package apt_project;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import opennlp.tools.namefind.NameFinderME; 
import opennlp.tools.namefind.TokenNameFinderModel; 
import opennlp.tools.util.Span;

public class Trends {
	
	public static String[] stringArrayFromStringArrayList(ArrayList<String> arr) 
    { 
        String str[] = new String[arr.size()]; 
        for (int j = 0; j < arr.size(); j++) { 
            str[j] = arr.get(j); 
        } 
        return str; 
    } 
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String prevCountryChosen = new String("United States");
		String currCountryChosen = new String("United States");
		InputStream is = new FileInputStream("en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();
        NameFinderME nameFinder = new NameFinderME(model);
       	String query = new String("Has Hillary Clinton contracted coronavirus?");
       	String[] splitQuery = query.split("\\s");
        Set <String> trends = new HashSet<>();
		@SuppressWarnings("resource")
		Scanner s = new Scanner(System.in);
		String temp = new String();
		while (true)
		{
			////////////////////////////////////////
	        Span nameSpans[] = nameFinder.find(splitQuery);
	        // nameSpans contain all the possible entities detected
	        for(Span span: nameSpans){
	        	temp = "";
	        	for(int index=span.getStart();index<span.getEnd();index++){
	        		if(index == span.getEnd() - 1)
	        			temp = temp + splitQuery[index];
	        		else
	        			temp = temp + splitQuery[index] + " ";
	            }
	        	trends.add(temp);
	        }
	        
	        System.out.println("Trends for " + currCountryChosen + ": " + trends);
	        ////////////////////////////////////////////////////////
			
			
			System.out.println("Please choose country. (In web-interface, this is the combo box for country, if it doesn't change, the while loop continues)");
			if (s.hasNextLine())
			{
				currCountryChosen = s.nextLine();
			}
			
			if(!prevCountryChosen.equals(currCountryChosen))
			{
				trends.clear();
				prevCountryChosen = currCountryChosen;
			}
			
			System.out.println("Please enter another query.");
			if (s.hasNextLine())
			{
				query = s.nextLine();
			}
			splitQuery = query.split("\\s");
		}
		
	}

}
