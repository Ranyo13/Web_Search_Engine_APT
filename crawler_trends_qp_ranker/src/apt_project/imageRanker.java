package apt_project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class imageRanker {
    
    imageRanker() {}
    
    class sorter implements Comparator<imgResult> { 
        @Override
        public int compare(imgResult o1, imgResult o2) {
            return o2.gettotRank().compareTo(o1.gettotRank());         
        }
    } 

    void rank(Connection conn,String[] words , List<imgResult> images) throws SQLException{

        int queryWords = words.length;
        if(queryWords > 0) {
            try {
                images.clear(); // just for caution
                java.sql.Statement myStmt;
                java.sql.Statement myStmt2;
                java.sql.Statement myStmt3;
                ResultSet myRs = null;
                ResultSet myRs2 = null;
                ResultSet myRs3 = null;
                myStmt = conn.createStatement();
                myStmt2 = conn.createStatement();
                myStmt3 = conn.createStatement();

                String[] actualWords = new String[queryWords];
            
                double[] IDF = new double[queryWords];
                double  valueIDF; // temp for calculating idf for each word
                int ifKeyWords = 0; // to find out if none of the keywords were found in the database
                //getting idf for each word
                for(int key=0;key<queryWords;key++) {
                    myRs = myStmt.executeQuery("select idf from imgidf where word = '"+ words[key] +"';");
                    if(myRs.next()) {

                            valueIDF = myRs.getDouble("idf"); 
                            IDF[ifKeyWords] = valueIDF;
                            actualWords[ifKeyWords] = words[key];
                            ifKeyWords = ifKeyWords + 1; // actual number of querywords to be processed
                    } // else not in our database
                }
                if(ifKeyWords > 0) { // continue if at least one keyword is in the database
                    // getting ids (distinct) of all images that contain any of the keywords
                    String query1 = "select DISTINCT id from imagewords where word IN (";
                    String query = "";
                    for(int i =0 ;i<ifKeyWords;i++){
                        query = query + "'" +actualWords[i] + "'" + ",";
                    }
                    query1 = query1 + query;
                    query1 = query1.substring(0, query1.length()-1);
                    query1 = query1 + ");";

                    myRs = myStmt.executeQuery(query1); // getting the ids
                    double addedRank = 0.0; // to calculate tf-idf for each doc
                    double TF; // to calculate normalized tf for each doc/word
                    String id; // to loop over ids that contain any of the keywords
                    int length; // length of the document
                    String url; // url of the image
                    int numOccur; // frequency of a word in a doc in whole body
                    while(myRs.next()) { // while there is an id to calculate rank for
                        
                        
                        id = myRs.getString("id"); // that id
                        // getting that id's info
                        // currently only using length,country,date but maybe in the future will use also link,description 
                        //!!!!!!!!!!!!!!!!!!!!!!
                        // if menna wants

                        
                        myRs2 = myStmt2.executeQuery("select * from images where id = "+id+";");

                        if(myRs2.next()) {
                            length = myRs2.getInt("length");
                            url = myRs2.getString("url");
                            if (length>0) {
                                for(int key=0;key<ifKeyWords;key++) {
                                    myRs2 = myStmt2.executeQuery("select count from imagewords where id = "+id+" and word = '"+actualWords[key]+"';");
                                    if(myRs2.next()) {
                                        numOccur = myRs2.getInt("count");
                                    } else {
                                        numOccur = 0;
                                    }
                                    TF = (double)numOccur/(double)length;
                                    // accumulate tf-idf
                                    addedRank = addedRank + TF * IDF[key];
                                }
                            } else {// end for each keyword
                                System.out.println("Caution length of id : "+id+" was found 0");
                            }
                            images.add(new imgResult(Integer.parseInt(id),addedRank,url));
                            addedRank = 0.0;


                        } else {
                            System.out.printf("Error in database id : %s was not found in links table\n",id);
                        } // end retreiving values for one id

                    }//end while there is an id to calculate rank for
                    Collections.sort(images, new sorter());
                    System.out.println(images);
                } else {
                    System.out.printf("your seach keywords didn't have any results\n");
                }
                if(myRs!=null) {
                    myRs.close();
                }
                if(myRs2!=null) {
                    myRs2.close();
                }
                if(myRs3!=null) {
                    myRs3.close();
                }
                myStmt.close();
                myStmt2.close();
                myStmt3.close();
            } catch (SQLException ex) {
                Logger.getLogger(Ranker.class.getName()).log(Level.SEVERE, null, ex);
            }  
        } // supposed to be handled in literally anywhere else 
        else {
            System.out.println("User didn't enter any search words");
        }
    }    
}