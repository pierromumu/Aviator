package tools;

import command.*;

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

    // retrait des caractères parasites d'une chaîne
    public static String cleanTerm(String term){

        String cleanedTerm;
        cleanedTerm = term.toLowerCase();
        cleanedTerm = cleanedTerm.replaceAll("[éèê]", "e");
        cleanedTerm = cleanedTerm.replaceAll("[îì]", "i");
        cleanedTerm = cleanedTerm.replaceAll("[ùûï]", "u");
        cleanedTerm = cleanedTerm.replaceAll("[àâä]", "a");

        return stemTerm(cleanedTerm.replaceAll("[\\W]",""));
    }

    // vérification de stop word
    public static boolean processTerm(String term){
        if(stopWords.contains(term))
            return false;
        else
            return true;
    }

    // obtention du nombre de documents
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

    // affichage de l'indice de précision pour chaque document
    public static void displayResults(HashMap<Integer, Double> results){
        System.out.println("Doc ID\t| Accuracy");
        for (Map.Entry<Integer, Double> e : results.entrySet()){
            System.out.println(e.getKey()+"\t\t| "+e.getValue());
        }
    }

    public static ArrayList<Integer> orderResults(HashMap<Integer, Double> results) {
        ArrayList<Integer> docs = new ArrayList<>(results.keySet());

        // trie à bulles appliqué
        for (int index = docs.size() -1 ; index > -1; index--){
            for (int j = 0; j < index; j++){
                if (results.get(docs.get(j+1)) > results.get(docs.get(j))){
                    int temp = docs.get(j);
                    docs.set(j, docs.get(j+1));
                    docs.set(j+1, temp);
                }
            }
        }

        return docs;
    }

    public static void displayOrderedResults(HashMap<Integer, Double> results){

        ArrayList<Integer> docs = orderResults(results);

        System.out.println("Doc ID\t| Accuracy");
        for (int i : docs){
            System.out.println(i+"\t\t| "+results.get(i));
        }
    }

    // comparaison des résultats obtenus à ceux attendus dans les fichiers qrels
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

    // décompte des entrées de la HashMap
    public static int getNbPertinent(HashMap<Integer, Boolean> doc){

        int result = 0;

        for (int i : doc.keySet()){
            if (doc.get(i)){
                result += 1;
            }
        }

        return result;
    }

    // affichage du tableau comparatif bien formaté
    public static void displayFinalComparison(HashMap<Integer, Boolean> resultsToReach, HashMap<Integer, Double> results){

        // récupération du résultat préalablement ordonné
        ArrayList<Integer> docs = orderResults(results);

        System.out.println("Doc ID\t|\t\tAccuracy\t\t\t\t\t|\t\tExpected\t|\t\tOK/KO");
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

                System.out.println(i+"\t\t|\t\t"+results.get(i)+"\t\t\t|\t\t"+resultsToReach.get((i)) + "\t\t|\t\t"+verif);
            } else {
                System.out.println(i+"\t\t|\t\t"+results.get(i)+"\t\t\t|\t\tNO DATA\t\t|\t\tOK (NO DATA)");
            }

        }

        System.out.println("Errors count : " + errors);
        System.out.println("Incriminated documents : " + errorList);
    }

    // calcul du rappel pour le graphe
    public static float recall(ArrayList<Integer> orderedResults, HashMap<Integer, Boolean> comparison, int max, int nbPert){

        if (max > orderedResults.size()){
            max = orderedResults.size();
        }

        int id;
        float result = 0;

        for (int i = 0; i < max; i++){
            id = orderedResults.get(i);
            if (comparison.containsKey(id) && comparison.get(id)){
                result += 1;
            }
        }

        result = (result / (float) nbPert);

        return result;
    }

    // calcul de la précision pour le graphe
    public static float accuracy(ArrayList<Integer> orderedResults, HashMap<Integer, Boolean> comparison, int max){

        float result = 0;

        if (max > orderedResults.size()){
            max = orderedResults.size();
        }

        int id;

        for (int i = 0; i < max; i++){
            id = orderedResults.get(i);
            if (comparison.containsKey(id) && comparison.get(id)){
                result += 1;
            }
        }

        result = (result / (float) max);

        return result;

    }

}
