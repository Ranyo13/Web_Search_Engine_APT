package apt_project;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler implements Runnable {

	private static Set <URL> links = new HashSet<>();
	private Set <URL> urlsToBeCrawled;
	private final static int stoppingCriteria = 5000;
	static Connection conn;
//	static Integer numberOfThreads = 0;
//	static Integer maxNumberOfThreads;
	
	Crawler(Set <URL> inputUrls) //CONSTRUCTOR
	{
		urlsToBeCrawled = new HashSet<>();
		urlsToBeCrawled.addAll(inputUrls);
	}

	public static boolean checkRobots(URL link) //FUNCTION TO CHECK FOR ROBOTS.TXT (JSOUP IS USED TO PARSE HTML, CAN'T BE USED HERE)
	{
		try {
			URL newUrl = new URL(link.getProtocol(), link.getHost(), link.getPort(), "/robots.txt");
			
		    
			BufferedReader in2 = new BufferedReader(new InputStreamReader(newUrl.openStream()));
		    String line2 = null;
		    
		    while((line2 = in2.readLine()) != null) {
		        if(line2.contains("Allow: "))
		        {
		        	int pos = 0;
			    	line2.indexOf("Allow: ", pos);
			    	pos = pos + "Allow: ".length();
			    	String temp = line2.substring(pos);
		        	if(temp.contains("*"))
			        {
			        	String opt=temp.replace("*", ".*");
			        	Pattern p = Pattern.compile(opt);
			        	Matcher m = p.matcher(link.getPath());
			        	boolean b = m.find();
			        	if(b==true) { 
			        		return true; 
			        		}
			        } 
		        	else if(temp.contains(")"))
			        {
			        	String opt=temp.replace(")", "//)");
			        	Pattern p = Pattern.compile(opt);
			        	Matcher m = p.matcher(link.getPath());
			        	boolean b = m.find();
			        	if(b==true) { 
			        		return true; 
			        	}
			        }
		        	else
		        	{
			        	Pattern p = Pattern.compile(temp);
			        	Matcher m = p.matcher(link.getPath());
			        	boolean b = m.find();
			        	if(b==true) {
			        		return true; 
			        		
			        	}
		        	}
		        }
		        	        
			}
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(newUrl.openStream()));
		    String line = null;
		    while((line = in.readLine()) != null) {
		        if(line.contains("Disallow: "))
		        {
		        	int pos = 0;
			    	line.indexOf("Disallow: ", pos);
			    	pos = pos + "Disallow: ".length();
			    	String temp = line.substring(pos);
		        	if(temp.contains("*"))
			        {
			        	String opt=temp.replace("*", ".*");
			        	Pattern p = Pattern.compile(opt);
			        	Matcher m = p.matcher(link.getPath());
			        	boolean b = m.find();
			        	if(b==true) { 
			        		return false; 
			        	}
			        }
		        	else if(temp.contains(")"))
			        {
			        	String opt=temp.replace(")", "//)");
			        	Pattern p = Pattern.compile(opt);
			        	Matcher m = p.matcher(link.getPath());
			        	boolean b = m.find();
			        	if(b==true) { 
			        		return false; 
			        	}
			        }
		        	else
		        	{
			        	Pattern p = Pattern.compile(temp);
			        	Matcher m = p.matcher(link.getPath());
			        	boolean b = m.find();
			        	if(b==true) { 
			        		return false; 
			        	}
		        	}
		        }
		        	        
			}
		    
		} catch (IOException e)  {
	        return false;
	    }
		return true;
	}
	
	void crawl(Set <URL> urls) //START CRAWLING RECURSIVELY WHEN CALLED
	{	
		PreparedStatement ps;
		String query;
		Document doc;
		Elements recLinks;
		String urlTextFromRecLinks;
		URL actualUrl;
		try {
			Statement s = conn.createStatement();
			ResultSet r = s.executeQuery("select count(*) from links");
			while(r.next())
			{
				int numberOfLinksRows = r.getInt(1);
				if(numberOfLinksRows > 5000)
				{
					System.out.println("Stopping criteria is met.");
					return;
				}
			}
		}
		catch (Exception e)
		{
			
		}
		if(!urls.isEmpty())
		{
			synchronized(links)
			{
				urls.removeAll(links);
				links.addAll(urls);
			}
			for(URL urlToBeInserted : urls)
			{
				try 
				{
					ps=conn.prepareStatement("insert into links values(?)");
					ps.setString(1, urlToBeInserted.toString());
					ps.executeUpdate();
					query = "delete from urlsToBeCrawled where link = ?";
					ps = conn.prepareStatement(query);
					ps.setString(1, urlToBeInserted.toString());
					ps.execute();
				} 
				catch(SQLException e)
				{
					continue;
				}
			}
			try {
				Statement s = conn.createStatement();
				ResultSet r = s.executeQuery("select count(*) from links");
				while(r.next())
				{
					int numberOfLinksRows = r.getInt(1);
					if(numberOfLinksRows > 5000)
					{
						System.out.println("Stopping criteria is met.");
						return;
					}
				}
			}
			catch (Exception e)
			{
				
			}
//			synchronized(links)
//			{
//				if (links.size()>=stoppingCriteria)
//				{
//					System.out.println("Stopping criteria is met.");
//					return;
//				}
//			}
			Set <URL> recUrls = new HashSet <>();
			for(URL url : urls) 
			{
				System.out.println(url);
				try {
					doc = Jsoup.connect(url.toString()).get();
					recLinks = doc.select("a[href]");
					for(Element element : recLinks)
					{
						urlTextFromRecLinks = element.attr("abs:href"); // Absolute url text
						//System.out.println(urlTextFromRecLinks);	
						try
						{
							actualUrl = new URL (urlTextFromRecLinks);
							if(checkRobots(actualUrl))
							{
								recUrls.add(actualUrl);
								try {
									ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
									ps.setString(1, urlTextFromRecLinks);
									ps.executeUpdate();
								}
								catch(Exception |Error e)
								{
									continue;
								}
							}
						}
						catch (Exception e)
						{
							
						}
						
					}
				}
				catch (Exception e)
				{
					
				}
				
				crawl(recUrls);
			}
		}
	}

	@Override
	public void run() {
		crawl(urlsToBeCrawled);
		System.out.println(links.size() + " links have been crawled successfully.");
	}

	public static void main(String[] args) throws MalformedURLException{
		
		//CONNECTING TO DATABASE
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
		
		
		

//		maxNumberOfThreads = numberOfThreads;
		//ArrayList <URL> seedSet = new ArrayList<URL>();
		ResultSet rs;
		String query = "SELECT * FROM links";
		String query2 = "SELECT * FROM urlsToBeCrawled";
		int linksCount=0;
		int seedSetLimit=0;
		//CODE THAT TAKES SEED SET FROM URLS TO BE CRAWLED TABLE AND HANDLES CRAWLER INTERRUPTION
		//DESIGN: WHEN INTERRUPTED, DIVIDE URLS PRESENT IN URLS TO BE CRAWLED TABLE ON THREADS 
		ArrayList <URL> seedSet = new ArrayList <URL>();
		try
		{
		    Statement st = conn.createStatement(); 
		    // execute the query, and get a java resultset
		    rs = st.executeQuery(query);
		    while(rs.next())
		    {
		    	links.add(new URL(rs.getString("link")));
		    	linksCount++;
		    }
		    if (linksCount>=stoppingCriteria)
		    {
		    	System.out.println("Database contains more than 5000 links. Aborting.");
		    	return;
		    }
		    if (linksCount>0)
		    	System.out.println(linksCount + " links added to the crawler memory from database after being interrupted.");
		    Statement st2 = conn.createStatement();
		    rs = st2.executeQuery(query2);
		    while(rs.next())
		    {
		    	seedSet.add(new URL(rs.getString("link")));
		    	seedSetLimit++;
		    }
		    
	    	if(seedSetLimit==0)
			{
				PreparedStatement ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://edition.cnn.com/");
				ps.executeUpdate();
				seedSet.add(new URL("https://edition.cnn.com/"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://www.bbc.com/news");
				ps.executeUpdate();
				seedSet.add(new URL("https://www.bbc.com/news"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://news.sky.com/");
				ps.executeUpdate();
				seedSet.add(new URL("https://news.sky.com/"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://abcnews.go.com/International/");
				ps.executeUpdate();
				seedSet.add(new URL("https://abcnews.go.com/International/"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://www.theguardian.com/world");
				ps.executeUpdate();
				seedSet.add(new URL("https://www.theguardian.com/world"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://globalnews.ca/world/");
				ps.executeUpdate();
				seedSet.add(new URL("https://globalnews.ca/world/"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://www.nytimes.com/section/world");
				ps.executeUpdate();
				seedSet.add(new URL("https://www.nytimes.com/section/world"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://www.nbcnews.com/world");
				ps.executeUpdate();
				seedSet.add(new URL("https://www.nbcnews.com/world"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://www.reuters.com/news/archive/worldNews");
				ps.executeUpdate();
				seedSet.add(new URL("https://www.reuters.com/news/archive/worldNews"));
				
				ps=conn.prepareStatement("insert into urlsToBeCrawled values(?)");
				ps.setString(1, "https://www.telegraph.co.uk/news/world/");
				ps.executeUpdate();
				seedSet.add(new URL("https://www.telegraph.co.uk/news/world/"));
				
				System.out.println("Inserted initial seed set into database.");
				seedSetLimit=10;
			}
	    	if(!seedSet.get(0).toString().equals("https://edition.cnn.com/"))
	    		System.out.println(seedSetLimit + " links were scheduled to be crawled but an interruption occurred. Resuming.");
		}
	    catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
		System.out.println("Please enter number of threads for crawler:"
				+ " (If a number greater than " + seedSetLimit + " is entered, " + seedSetLimit + " threads will be created)");
		int numberOfThreads;
		Scanner s = new Scanner(System.in);
		numberOfThreads = s.nextInt();
		s.close();
		if(numberOfThreads>seedSetLimit)
			numberOfThreads = seedSetLimit;
//		seedSet.add(new URL ("https://edition.cnn.com/"));
//		seedSet.add(new URL ("https://www.bbc.com/news"));
//		seedSet.add(new URL ("https://news.sky.com/"));
//		seedSet.add(new URL ("https://abcnews.go.com/International/"));
//		seedSet.add(new URL ("https://www.theguardian.com/world"));
//		seedSet.add(new URL ("https://globalnews.ca/world/"));
//		seedSet.add(new URL ("https://www.nytimes.com/section/world"));
//		seedSet.add(new URL ("https://www.nbcnews.com/world"));
//		seedSet.add(new URL ("https://www.reuters.com/news/archive/worldNews"));
//		seedSet.add(new URL ("https://www.telegraph.co.uk/news/world/"));	
		int quotient = seedSetLimit/numberOfThreads;
		int remainder = seedSetLimit%numberOfThreads;
		// seedSetLimit = 3 / number of threads = 2 //// result = 1 q + 1 r
		// i = 0; i < 3; i+= 1
		// j = 0; j < 1; j++
		int threadCounter = 0;
		int linksThreadStartedWith = 0;
		boolean remainderDone=false;
		for(int i = 0; i < seedSetLimit; i+=quotient)
		{
			if (remainderDone)
				break;
			linksThreadStartedWith = 0;
			Set <URL> threadURLs = new HashSet <>();
			if((threadCounter == numberOfThreads - 1) && remainder>0)
			{
				for(int j = i; j < i + remainder; j++)
				{
					threadURLs.add(seedSet.get(j));
					linksThreadStartedWith++;
					remainderDone=true;
				}
			}
			else
			{
				for(int j = i; j < i + quotient; j++)
				{
					threadURLs.add(seedSet.get(j));
					linksThreadStartedWith++;
				}
			}
			Thread t = new Thread(new Crawler(threadURLs));
			threadCounter++;
			System.out.println("Thread " + threadCounter + " started with " + linksThreadStartedWith + " links as an initial seed set");
			System.out.println(threadURLs);
			t.start();
		}
	}
}