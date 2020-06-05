package apt_project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ranker {
    
    class sorter implements Comparator<webResult> { 
        @Override
        public int compare(webResult o1, webResult o2) {
            return o2.gettotRank().compareTo(o1.gettotRank());         
        }
    } 

    Ranker() {}
    
    void rank(Connection conn,String[] words , List<webResult> docs , String userCountry) throws SQLException{

        int queryWords = words.length;
        
        if(queryWords > 0) {

            try {

                docs.clear(); // just for caution

                java.sql.Statement myStmt;
                java.sql.Statement myStmt2;
                java.sql.Statement myStmt3;
                ResultSet myRs = null;
                ResultSet myRs2 ;
                ResultSet myRs3 = null;
                myStmt = conn.createStatement();
                myStmt2 = conn.createStatement();
                myStmt3 = conn.createStatement();


                //int numKey = keywords.length;
                String[] actualWords = new String[queryWords];

                Double relW = 50.0;
                Double titleW = 3.0;
                Double hedW = 2.0;
                Double countryW = 3.0;
                Double dateW = 3.0;
                Double prW = 39.0;
                Double prMax;

                myRs2 = myStmt2.executeQuery("select max(pR) as maximum from pageRank;");

                if(myRs2.next()) {// if page rank was successful and bring back max pageRank

                    prMax =  myRs2.getDouble("maximum");

                    Double maxRank; 
                    myRs = myStmt.executeQuery("select count(DISTINCT id) from links;");

                    if(myRs.next() && prMax > 0) { // if there is a count of docs in database

                        int totalDocCount = myRs.getInt("count(DISTINCT id)");

                        if(totalDocCount > 0) {
                            // caclulating max rank of tf-idf
                            // (tf/length of document has max value of 1 ,
                            // idf has max value of log10(total number of documents) 
                            // and then multiply this max tf-idf by number of words)
                            maxRank = (double)queryWords * Math.log10((double)totalDocCount);
                            double[] IDF = new double[queryWords];
                            double  valueIDF; // temp for calculating idf for each word
                            int ifKeyWords = 0; // to find out if none of the keywords were found in the database

                            //getting idf for each word
                            for(int key=0;key<queryWords;key++) {
                                myRs = myStmt.executeQuery("select idf from docidf where word = '"+ words[key] +"';");
                                if(myRs.next()) {
                                    
                                        valueIDF = myRs.getDouble("idf"); 
                                        IDF[ifKeyWords] = valueIDF;
                                        actualWords[ifKeyWords] = words[key];
                                        ifKeyWords = ifKeyWords + 1; // actual number of querywords to be processed
                                } // else not in our database
                            }

                            if(ifKeyWords > 0) { // continue if at least one keyword is in the database

                                // getting ids (distinct) of all docs that contain any of the keywords
                                String query1 = "select DISTINCT id from body where word IN (";
                                String query = "";
                                for(int i =0 ;i<ifKeyWords;i++){
                                    query = query + "'" +actualWords[i] + "'" + ",";
                                }
                                
                                query1 = query1 + query;
                                query1 = query1.substring(0, query1.length()-1);
                                query1 = query1 + ");";
                                

                                myRs = myStmt.executeQuery(query1); // getting the ids

                                double addedRank = 0.0; // to calculate tf-idf for each doc
                                // my title and header rank are according to how many keywords appeared in them
                                // not how many times they appeared
                                double title = 0.0; // to calculate title rank
                                double header = 0.0; // to calculate header rank
                                int temp1; // to retreive count of header/title  
                                double TF; // to calculate normalized tf for each doc/word
                                String id; // to loop over ids that contain any of the keywords
                                int length; // length of the document
                                // my country rank is that if the country of the user matches the country of the doc
                                // this docs takes an extra countryW rank
                                // o.w. it is zero
                                String country; // country of the document
                                // my date rank is that any document older than 2015 or has no date will take zero dat score
                                // o.w it will take an extra dateW rank
                                int date; // date of publication of the document
                                int numOccur; // frequency of a word in a doc in whole body
                                String url; // url of the website
                                Double prV; // temp to get pageRank for doc
                                
                                String description;

                                while(myRs.next()) { // while there is an id to calculate rank for

                                    id = myRs.getString("id"); // that id


                                    // getting that id's info
                                    // currently only using length,country,date but maybe in the future will use also link,description 
                                    //!!!!!!!!!!!!!!!!!!!!!!
                                    // if menna wants

                                    myRs2 = myStmt2.executeQuery("select * from links where id = "+id+";");

                                    if(myRs2.next()) {

                                        length = myRs2.getInt("length");
                                        country = myRs2.getString("country");
                                        date = myRs2.getInt("date");
                                        url = myRs2.getString("link");
                                        description = myRs2.getString("description");

                                        // getting pageRank of this document
                                        myRs3 = myStmt3.executeQuery("select pR from pageRank where id ="+id+";");

                                        if(myRs3.next()) {
                                            
                                            prV = myRs3.getDouble("pR");
                                            if(prV > 0) {
                                                prV = (prV/prMax) * prW;
                                                if (length>0) {
                                                    
                                                    for(int key=0;key<ifKeyWords;key++) {

                                                        myRs3 = myStmt3.executeQuery("select header_count from header where id = "+id+" and word = '"+actualWords[key]+"';");

                                                        if(myRs3.next()) {

                                                            temp1 = myRs3.getInt("header_count");
                                                            if (temp1 > 0) {
                                                                header = header + 1.0;
                                                            }
                                                        }

                                                        myRs3 = myStmt3.executeQuery("select title_count from title where id = "+id+" and word = '"+actualWords[key]+"';");
                                                        if(myRs3.next()) {
                                                            temp1 = myRs3.getInt("title_count");
                                                            if (temp1 > 0) {
                                                                title = title + 1.0;
                                                            }
                                                        }
                                                        
                                                        myRs2 = myStmt2.executeQuery("select body_count from body where id = "+id+" and word = '"+actualWords[key]+"';");
                                                        if(myRs2.next()) {
                                                            numOccur = myRs2.getInt("body_count");

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

                                                addedRank = addedRank * relW / maxRank;
                                                title = (title/(double)queryWords) * titleW; // dividing by total number of querywords as that is the max
                                                header = (header/(double)queryWords) * hedW; // same as title

                                                if(date >= 2015) { 
                                                    addedRank = addedRank + dateW ;
                                                }
                                                if(userCountry.equals(country) && !userCountry.equals("")) {
                                                    addedRank = addedRank + countryW;
                                                }

                                                addedRank = addedRank + title + header + prV;
                                                docs.add(new webResult(Integer.parseInt(id),addedRank,url,description));
                                            } else {
                                                System.out.println("Caution pageRank of id : "+id+" was found 0");
                                            }
                                        } else {
                                            System.out.printf("Error in database id : %s was not found in pageRank table\n",id);
                                        }
                                        addedRank = 0.0;
                                        title = 0.0;
                                        header = 0.0;
                                    } else {
                                        System.out.printf("Error in database id : %s was not found in links table\n",id);
                                    } // end retreiving values for one id

                                }//end while there is an id to calculate rank for

                                Collections.sort(docs, new sorter());

                                System.out.println(docs);

                            } else {
                                System.out.printf("your seach keywords didn't have any results\n");
                            }
                            
                        } // also no results for the user
                        else {
                            System.out.println("Error num of documents  in database = 0");
                        }
                    } // also no results for the user
                    else {
                        if(prMax > 0 ) {
                            System.out.printf("Error in database max pageRank not > 0 !!!\n");
                        } else {
                            System.out.printf("Error in database no Documents found !!!\n");
                        }
                    }
                } // for the user no results
                else {
                    System.out.println("Error retreiving pageRank no results found !!!");
                }

                if(myRs!=null) {
                    myRs.close();
                }
                myRs2.close();
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
