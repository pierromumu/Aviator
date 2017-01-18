import index.*;
import command.*;
import query.*;
import requests.*;
import reword.Reword;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {

        Command.connect();

        // CODE POUR PEUPLER LA BASE DE DONNEES

        //Index.createIndex();

        // REQUETES

        Path requestsFile = Paths.get("./files/requetes.html");
        ArrayList<ArrayList<ArrayList<String>>> requests = new ArrayList<>();
        requests = Requests.parse(requestsFile);

        // AJOUT D'UNE NOUVELLE REQUETE BONUS ECRITE EN FRANCAIS DANS LE CODE (qrel1Q2 à modifier avec les résultats attendus)
        ArrayList<ArrayList<String>> q12 = new ArrayList<>();
        ArrayList<String> q12_tmp = new ArrayList<>();
        q12_tmp.add("récompense");
        q12_tmp.add("1.0");
        q12.add(q12_tmp);
        q12_tmp = new ArrayList<>();
        q12_tmp.add("enfant de Trappes");
        q12_tmp.add("1.0");
        q12.add(q12_tmp);
        q12_tmp = new ArrayList<>();
        requests.add(q12);
        System.out.println("");
        System.out.println(requests);

        ArrayList<ArrayList<ArrayList<String>>> editedRequests = Reword.transformWordsLists(requests);

        System.out.println("");

        for (int i =0; i < requests.size(); i++){
            System.out.println("** Original request n°"+(i+1)+" : "+requests.get(i));
            System.out.println("");
            Query q = null;
            try {
                q = new Query(requests.get(i), i+1);
            } catch (Exception e) {
                System.out.println("Error creating the query n°"+(i+1)+", check your input terms !");
            }
            q.execute(1);
            System.out.println("");
        }

        for (int i =0; i < editedRequests.size(); i++){
            System.out.println("*** Edited request n°"+(i+1)+" : "+editedRequests.get(i));
            System.out.println("");
            Query q = null;
            try {
                q = new Query(editedRequests.get(i), i+1);
            } catch (Exception e) {
                System.out.println("Error creating the edited query n°"+(i+1)+", check your input terms !");
            }
            q.execute(2);
            System.out.println("");
        }

        Query.displayAccuracyResultsOriginal();
        System.out.println("");
        Query.displayAccuracyResultsEdited();

        System.out.println("");
        System.out.println("All queries processed.");
    }
}
