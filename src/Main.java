import index.*;
import command.*;
import query.*;
import requests.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {

        Command.connect();

        //Index.createIndex();

        // EXEMPLE DE REQUETE

        Path requestsFile = Paths.get("./files/requetes.html");
        ArrayList<ArrayList<String>> requests = new ArrayList<>();
        requests = Requests.parse(requestsFile);

        for (int i =0; i < requests.size(); i++){
            //L'arraylist peut remplacer la liste manuelle d'après
            System.out.println(requests.get(i));
        }

        ArrayList<String> param = new ArrayList<>();
        param.add("personnes");
        param.add("Intouchables");
        Query q1 = null;
        try {
            q1 = new Query(param, 1);
        } catch (Exception e) {
            System.out.println("Error creating the query : check your input terms !");
        }
        q1.execute();
    }
}
