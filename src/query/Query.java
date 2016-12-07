package query;

import command.Command;
import tools.Tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class Query {

    private ArrayList<String> words;
    private ArrayList<Integer> wordsIDs;

    // identifiant du document et somme des occurrences des termes de la requête
    private HashMap<Integer, Integer> resultTF;
    // pour chaque terme multiplier le nombre d'occurrences par l'IDF puis sommer le tout
    private HashMap<Integer, Float> resultTFIDF;

    private HashMap<Integer, HashMap<Integer, Integer>> wordDocOcc;

    public Query(ArrayList<String> w) throws Exception {
        words = w;
        populateWordsIDs();
        if(wordsIDs.contains(null)) {
            throw new Exception();
        }
    }

    public void populateWordsIDs(){

        wordsIDs = new ArrayList<Integer>(words.size());

        for(String w : words){
            w = Tools.cleanTerm(w);

            if(Tools.processTerm(w)) {

                String sql = "SELECT ID FROM vocabulary WHERE WORD = '";
                sql = sql + w + "';";
                ResultSet call = Command.executeQ(sql);

                try {
                    if(call.next()){
                        wordsIDs.add(call.getInt(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Command.closeStmt();
            }
        }
    }

    public void execute(){

        resultTF = new HashMap<>();
        resultTFIDF = new HashMap<>();
        wordDocOcc = new HashMap<>();

        for(Integer i : wordsIDs){

            HashMap<Integer, Integer> temp = new HashMap<>();

            String sql = "";
            sql = "SELECT DOC_ID, OCC FROM invertedindex WHERE WORD_ID = " + i + ";";

            ResultSet call = Command.executeQ(sql);

            try {
                while(call.next()){

                    if (temp.containsKey(call.getInt(1))){
                        temp.put(call.getInt(1), call.getInt(2) + temp.get(call.getInt(1)));
                    } else {
                        temp.put(call.getInt(1), call.getInt(2));
                    }

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Command.closeStmt();

            wordDocOcc.put(i, temp);
        }

        for(Integer i : wordsIDs){
            for (Map.Entry<Integer,Integer> e : wordDocOcc.get(i).entrySet()) {

                if (resultTF.containsKey(e.getKey())){
                    resultTF.put(e.getKey(), e.getValue() + resultTF.get(e.getKey()));
                } else {
                    resultTF.put(e.getKey(), e.getValue());
                }
            }
        }

        System.out.println(resultTF);
    }
}
