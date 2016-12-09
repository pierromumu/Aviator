package query;

import command.Command;
import org.jfree.ui.RefineryUtilities;
import tools.Tools;
import graph.Graph;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;


public class Query {

    private ArrayList<String> words;
    private ArrayList<Integer> wordsIDs;

    // identifiant du document et somme des occurrences des termes de la requête
    private HashMap<Integer, Integer> resultTF;
    //<Doc_ID, Précision> en suivant l'alog IDF
    private HashMap<Integer, Integer> resultIDF;
    // pour chaque terme multiplier le nombre d'occurrences par l'IDF puis sommer le tout
    private HashMap<Integer, Integer> resultTFIDF;

    //wordDocOcc = <Word_ID, <Doc_ID, Occurences>>
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
        resultIDF = new HashMap<>();
        resultTFIDF = new HashMap<>();
        wordDocOcc = new HashMap<>();
        double totalDocs = Tools.getNumberEntries();

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

        //TF Hashmap

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

        //IDF Hashmap

        for(Integer i : wordsIDs){


            double nbDocs = wordDocOcc.get(i).size();
            double weight = Math.log(totalDocs/nbDocs);

            for (Map.Entry<Integer,Integer> e : wordDocOcc.get(i).entrySet()) {

                if (resultIDF.containsKey(e.getKey())){
                    resultIDF.put(e.getKey(), (int) Math.round(e.getValue()*weight) + resultIDF.get(e.getKey()));
                } else {
                    resultIDF.put(e.getKey(), (int) Math.round(e.getValue()*weight));
                }
            }
        }

        System.out.println(resultIDF);

        for (int key=1; key < (int) totalDocs +1; key ++){
            if (resultIDF.containsKey(key) && resultTF.containsKey(key)){
                resultTFIDF.put(key, resultTF.get(key)*resultIDF.get(key));
            } else if (resultIDF.containsKey(key)){
                resultTFIDF.put(key, resultIDF.get(key));
            } else if (resultTF.containsKey(key)){
                resultTFIDF.put(key, resultTF.get(key));
            } else {
                resultTFIDF.put(key, 0);
            }
        }

        Tools.displayOrderedResults(resultTFIDF);

        HashMap<Integer, Boolean> verif = Tools.getVerifiedResults(1);

        Tools.displayFinalComparison(verif, resultTFIDF);

        //HashMap<Integer, Boolean> orginalPertinence = Tools.

        ArrayList<Integer> orderedList = Tools.orderResults(resultTFIDF);

        int nbPert = Tools.getNbPertinent(verif);

        ArrayList<ArrayList<Double>> data = new ArrayList<>();

        ArrayList<Double> tempData;

        for (int sizeData = 1; sizeData< totalDocs +1; sizeData++){

            tempData = new ArrayList<>();

            float recall = Tools.recall(orderedList, verif, sizeData, nbPert);

            float accuracy = Tools.accuracy(orderedList, verif, sizeData);

            System.out.println("Data for "+sizeData+" elements: Recall " + recall + " | Accuracy: "+accuracy);

            tempData.add((double) recall);
            tempData.add((double) accuracy);
            data.add(tempData);

        }

        Graph graph = new Graph("Recall =f(Accuracy)", data);
        graph.pack();
        RefineryUtilities.centerFrameOnScreen(graph);
        graph.setVisible(true);


    }
}
