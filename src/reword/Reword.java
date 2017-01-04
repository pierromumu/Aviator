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

            ArrayList<String> tempTable = rawData.get(i);
            ArrayList<String> tempResult = new ArrayList<>();

            for (int j = 0; j < tempTable.size(); j++){
                String tempTerm = tempTable.get(j);
                tempResult.add(tempTerm);

                if (serverIsUp) {
                    query = "SELECT ?lab WHERE\n"
                            + "{\n"
                            + "    ?res <http://www.w3.org/2000/01/rdf-schema#label> \""+tempTerm+"\"@fr.\n"
                            + "    ?res <http://www.w3.org/2000/01/rdf-schema#label> ?lab."
                            + "}\n"
                            + "GROUP BY ?piece\n";
                    Iterable<Map<String, String>> rs = sparqlClient.select(query);
                    for (Map<String, String> r : rs) {
                        System.out.println(r.get("lab"));
                    }
                }
            }

            result.add(tempResult);
        }

        return result;
    }

}
