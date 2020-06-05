package apt_project;
public class webResult {
    
    public int id;
    public Double totRank;
    
    // this is only valid for websites
    public String url;
    public int phCount;
    public String description;
    

    // do not forget to initialize url
    // and description
    // and count
    
    
    public webResult(int id,Double totRank,String url,String description) {
        this.id = id;
        this.totRank = totRank;
        this.url = url;
        this.description = description;
    }

    public void setphCount(int phCount) {
        
    }
    
    @Override
    public String toString() { 
        return String.format("id : "+ id + " totRank : " + totRank+" phCount : " + phCount + "\n"); 
    }
    
    Double gettotRank() {
        return this.totRank;
    }
    
    String getURL() {
        
        return this.url;
    }   
}