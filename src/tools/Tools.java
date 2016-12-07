package tools;

import command.Command;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.CharArraySet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.*;


public class Tools {

    private static CharArraySet stopWords = FrenchAnalyzer.getDefaultStopSet();

    private static String stemTerm(String term){

        FrenchStemmer stemmer = new FrenchStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();

        return stemmer.getCurrent();
    }

    public static String cleanTerm(String term){

        String cleanedTerm;
        cleanedTerm = term.toLowerCase();

        return stemTerm(cleanedTerm.replaceAll("[\\W]",""));
    }

    public static boolean processTerm(String term){
        if(stopWords.contains(term))
            return false;
        else
            return true;
    }

    public static int getNumberEntries(){

        String sql = "SELECT count(ID) FROM documents;";

        ResultSet result = Command.executeQ(sql);
        int ret = -1;
        try {
            if(result.next()){
                ret = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Command.closeStmt();

        return ret;
    }

    public static void displayResults(HashMap<Integer, Integer> results){
        System.out.println("Doc ID\t| Accuracy");
        for (Map.Entry<Integer, Integer> e : results.entrySet()){
            System.out.println(e.getKey()+"\t\t| "+e.getValue());
        }
    }

    public static void displayOrderedResults(HashMap<Integer, Integer> results){
        ArrayList<Integer> docs = new ArrayList<>(results.keySet());

        //Ordering according to Bubble sort
        for (int index = docs.size() -1 ; index > -1; index--){
            for (int j = 0; j < index; j++){
                if (results.get(docs.get(j+1)) > results.get(docs.get(j))){
                    int temp = docs.get(j);
                    docs.set(j, docs.get(j+1));
                    docs.set(j+1, temp);
                }
            }
        }

        System.out.println("Doc ID\t| Accuracy");
        for (int i : docs){
            System.out.println(i+"\t\t| "+results.get(i));
        }

    }

    public static HashMap<Integer, Boolean> getVerifiedResults(int query){

            String documentName="qrelQ"+query;
            String textContent = "";
            Path file = Paths.get("./files/qrels/"+documentName+".txt");

            HashMap<Integer, Boolean> result = new HashMap<>();

            try {
                List<String> contentLines = Files.readAllLines(file, StandardCharsets.UTF_8);
                Iterator<String> iterator = contentLines.iterator();
                while (iterator.hasNext()) {
                    textContent = iterator.next();
                    Pattern p = Pattern.compile("^([^\\.]+)\\.html\\t(\\d)");
                    Matcher m = p.matcher(textContent);
                    if (m.find()){
                        boolean res = false;
                        if (m.group(2).equals("1")){
                            res = true;
                        }
                        String sql = "SELECT id FROM documents WHERE title='"+m.group(1)+"'";
                        ResultSet idSet = Command.executeQ(sql);
                        int id = -1;
                        try {
                            if (idSet.next()){
                                id = idSet.getInt(1);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (id != -1){
                            result.put(id, res);
                        }

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        System.out.println(result);

        return result;
    }

    public static void displayFinalComparison(HashMap<Integer, Boolean> resultsToReach, HashMap<Integer, Integer> results){

        //Copied code from ordered display
        ArrayList<Integer> docs = new ArrayList<>(results.keySet());

        //Ordering according to Bubble sort
        for (int index = docs.size() -1 ; index > -1; index--){
            for (int j = 0; j < index; j++){
                if (results.get(docs.get(j+1)) > results.get(docs.get(j))){
                    int temp = docs.get(j);
                    docs.set(j, docs.get(j+1));
                    docs.set(j+1, temp);
                }
            }
        }

        System.out.println("Doc ID\t|\tAccuracy\t|\tExpected\t|\tOK/KO");
        boolean verif;
        int errors = 0;
        ArrayList<Integer> errorList = new ArrayList<>();
        for (int i : docs){
            verif = false;
            if (resultsToReach.containsKey(i)){
                if ((resultsToReach.get(i) && results.get(i) > 0) || (!resultsToReach.get(i) && results.get(i) == 0)){
                    //Verif is true if we have an element and we are supposed to, or we don't and are supposed to.
                    verif = true;
                } else {
                    errors ++;
                    errorList.add(i);
                }
                System.out.println(i+"\t\t|\t"+results.get(i)+"\t\t\t|\t"+resultsToReach.get((i)) + "\t\t|\t"+verif);
            } else {
                System.out.println(i+"\t\t|\t"+results.get(i)+"\t\t\t|\tNO DATA\t\t|\tOK (NO DATA)");
            }

        }

        System.out.println("Number of errors: " + errors);
        System.out.println("Errors: " + errorList);
    }

}
