package apt_project;

public class imgResult {

    public int id;
    public Double totRank;
    public String url;

    public imgResult(int id,Double totRank,String url) {
    this.id = id;
    this.totRank = totRank;
    this.url = url;
    }
    
    @Override
    public String toString() { 
        return String.format("id : "+ id + " totRank : " + totRank+"\n"); 
    }
    
    Double gettotRank() {
        return this.totRank;
    }
}