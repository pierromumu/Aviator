package query;

import command.*;
import tools.*;
import graph.*;

import org.jfree.ui.RefineryUtilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;


public class Query {

    private ArrayList<String> words;
    private ArrayList<Integer> wordsIDs;
    private int queryId;
    static private HashMap<Integer, ArrayList<Float>> accuracyResults = new HashMap<>();

    // identifiant du document et somme des occurrences des mots de la requête
    private HashMap<Integer, Integer> resultTF;
    // identifiant du terme et valeur obtenue en suivant l'algorithme de calcul de l'IDF
    private HashMap<Integer, Double> resultIDF;
    // pour chaque terme multiplication du nombre d'occurrences par l'IDF puis somme sur le tout
    private HashMap<Integer, Double> resultTFIDF;

    // stockage des résultats d'interoogation sous la forme <WORD_ID, <DOC_ID, OCC>>
    private HashMap<Integer, HashMap<Integer, Integer>> wordDocOcc;

    public Query(ArrayList<String> w, int id) throws Exception {
        words = w;
        populateWordsIDs();
        if(wordsIDs.contains(null)) {
            throw new Exception();
        }
        queryId = id;
    }

    public static void displayAccuracyResults(){
        int[] sizeData = {5, 10, 25};
        for (int key:accuracyResults.keySet()){
            for (int index = 0; index < accuracyResults.get(key).size(); index ++){
                System.out.println("Request #"+key+" -- Data for "+sizeData[index]+" elements: Accuracy: "+accuracyResults.get(key).get(index));
            }

        }



    }

    // mise en correspondance des mots avec les identifiants dans la BD
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

    // exécution de la requête et affichage des résultats
    public void execute(){

        resultTF = new HashMap<>();
        resultIDF = new HashMap<>();
        resultTFIDF = new HashMap<>();
        wordDocOcc = new HashMap<>();
        int totalDocs = Tools.getNumberEntries();

        ArrayList<Float> accuracyTable = new ArrayList<>();

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

            sql = "SELECT ID FROM documents;";
            ResultSet callD = Command.executeQ(sql);

            try {
                while(callD.next()) {

                    if (!(temp.containsKey(callD.getInt(1))))
                        temp.put(callD.getInt(1), 0);

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Command.closeStmt();

            wordDocOcc.put(i, temp);
        }

        // remplissage de la HashMap TF

        for(Integer i : wordsIDs){
            for (Map.Entry<Integer,Integer> e : wordDocOcc.get(i).entrySet()) {

                if (resultTF.containsKey(e.getKey())){
                    resultTF.put(e.getKey(), e.getValue() + resultTF.get(e.getKey()));
                } else {
                    resultTF.put(e.getKey(), e.getValue());
                }
            }
        }

        //System.out.println(resultTF);

        // remplissage de la HashMap IDF

        for(Integer i : wordsIDs){

            int nbDocs = 0;

            for(int k : wordDocOcc.get(i).keySet()){
                if(wordDocOcc.get(i).get(k) > 0)
                    nbDocs=nbDocs+1;
            }

            double weight = Math.log10((double)totalDocs/(double)nbDocs);

            if (!(resultIDF.containsKey(i))){
                resultIDF.put(i, weight);
            }

        }

        //System.out.println(resultIDF);

        // remplissage de la HashMap TFIDF

        for(Integer i : wordsIDs){
            for (Map.Entry<Integer,Integer> e : wordDocOcc.get(i).entrySet()) {

                if (resultTFIDF.containsKey(e.getKey())){
                    resultTFIDF.put(e.getKey(), e.getValue()*resultIDF.get(i) + resultTFIDF.get(e.getKey()));
                } else {
                    resultTFIDF.put(e.getKey(), e.getValue()*resultIDF.get(i));
                }
            }
        }

        //System.out.println(resultTFIDF);

        //Tools.displayOrderedResults(resultTFIDF);

        HashMap<Integer, Boolean> verif = Tools.getVerifiedResults(this.queryId);

        Tools.displayFinalComparison(verif, resultTFIDF);

        ArrayList<Integer> orderedList = Tools.orderResults(resultTFIDF);

        int nbPert = Tools.getNbPertinent(verif);

        ArrayList<ArrayList<Double>> data = new ArrayList<>();

        ArrayList<Double> tempData;

        for (int sizeData = 1; sizeData <= totalDocs; sizeData++){

            tempData = new ArrayList<>();

            float recall = Tools.recall(orderedList, verif, sizeData, nbPert);
            float accuracy = Tools.accuracy(orderedList, verif, sizeData);

            //System.out.println("Data for "+sizeData+" elements: Recall " + recall + " | Accuracy: "+accuracy);

            //HashMap: <ID requête, [Précision à 5, Précision à 10, Précision à 25]>

            if (sizeData == 5){
                accuracyTable.add(accuracy);
            } else if (sizeData == 10){
                accuracyTable.add(accuracy);
            } else if (sizeData == 25){
                accuracyTable.add(accuracy);
            }

            tempData.add((double) recall);
            tempData.add((double) accuracy);
            data.add(tempData);

        }

        accuracyResults.put(this.queryId, accuracyTable);

        /*Graph graph = new Graph("Recall =f(Accuracy)", data, queryId);
        graph.pack();
        RefineryUtilities.centerFrameOnScreen(graph);
        graph.setVisible(true);*/



    }
}
