package reword;

import sparqlclient.SparqlClient;

import java.util.ArrayList;
import java.util.Map;

public class Reword {

    public static ArrayList<ArrayList<String>> transformWordsLists(ArrayList<ArrayList<String>> rawData){

        SparqlClient sparqlClient = new SparqlClient("localhost:3030/cinema");
        String query = "ASK WHERE { ?s ?p ?o }";
        boolean serverIsUp = sparqlClient.ask(query);

        ArrayList<ArrayList<String>> result = new ArrayList<>();

        for (int i = 0; i < rawData.size(); i++){

            System.out.println("Request #"+(i+1));

            ArrayList<String> tempTable = rawData.get(i);
            ArrayList<String> tempResult = new ArrayList<>();

            System.out.println("Raw data: " + tempTable);

            for (int j = 0; j < tempTable.size(); j++){
                String tempTerm = tempTable.get(j);
                tempResult.add(tempTerm);

                System.out.println("Raw word: "+tempTerm);

                if (serverIsUp) {
                    query = "SELECT ?lab WHERE\n"
                            + "{\n"
                            + "    {?res <http://www.w3.org/2000/01/rdf-schema#label> \""+tempTerm+"\"@fr.\n"
                            + "    ?res <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                            + " UNION {?res <http://www.w3.org/2000/01/rdf-schema#label> \""+tempTerm+"\".\n"
                            + "    ?res <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                            + "}\n";
                    //System.out.println(query);
                    Iterable<Map<String, String>> rs = sparqlClient.select(query);
                    for (Map<String, String> r : rs) {
                        System.out.print("Word to add: "+r.get("lab"));

                    //Ajout du mot; on vérifie ensuite qu'il ne soit pas identique à celui que l'on vient de chercher.

                        if (!tempTerm.equals(r.get("lab"))){
                            System.out.println(" -- Added");
                            tempResult.add(r.get("lab"));
                        } else {
                            System.out.println(" -- Removed");
                        }
                    }
                }
            }

            //Tester les relations entre les mots deux à deux.







            System.out.println("Added result: " + tempResult);

            result.add(tempResult);
        }

        return result;
    }

}
