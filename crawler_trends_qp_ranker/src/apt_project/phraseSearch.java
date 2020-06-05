package apt_project;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.apache.commons.lang.StringUtils;


public class phraseSearch {
    
    public phraseSearch() {}

    class phSorter implements Comparator<webResult> { 
        @Override
        public int compare(webResult o1, webResult o2) {

            Double r1 = Math.round(o1.totRank/10.0)*10.0;
            Double r2 = Math.round(o2.totRank/10.0)*10.0;

            int x = r2.compareTo(r1);           
            if (x != 0) {
                return x;
            }
            // x = 0 : 
            Integer x1 = o1.phCount;
            Integer x2 = o2.phCount;

            int y = x2.compareTo(x1);  

            if(y !=0) {
                return y;
            }
            return o2.gettotRank().compareTo(o1.gettotRank());     
        }
    }
        
        
    public String getBody(Document doc) {
        
        String text = "";
        Elements tags = doc.select("*");
        for (Element tag : tags) {
            for (TextNode tn : tag.textNodes()) {
                String tagText = tn.text().trim();
                if (tagText.length() > 0) {
                    text += tagText + " ";
                }
            }
        }
        return text.toLowerCase();
        
    }    
    
    
    // user Query : to search on the exact string
    // results : is the ranked results from ranker to rerank them 
    public void RankWebs(String userQuery, List<webResult> results) throws IOException { //, SQLException {
        String text;
        Document doc;
        int count;
        for (webResult temp : results) {

            try {

                doc = Jsoup.connect(temp.getURL()).get();
                text = getBody(doc);
                count = StringUtils.countMatches(text.toLowerCase(), userQuery.toLowerCase());
                System.out.println(count);  

                // setting the count of found phrases in the doc 
                temp.phCount = count;
            } catch (IOException ex ) {
                Logger.getLogger(phraseSearch.class.getName()).log(Level.SEVERE, null, ex);
                temp.phCount = 0;
            }  
        }
        System.out.println(results);
        // this is to remove any results that has no occurrences of the exact phrase
        results.removeIf(s -> s.phCount == 0);
        Collections.sort(results, new phSorter());
        System.out.println(results); 
    }
}

