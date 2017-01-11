package reword;

import sparqlclient.SparqlClient;

import java.util.ArrayList;
import java.util.Map;

public class Reword {

    public static String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c= new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private static Iterable<Map<String, String>> getRelatedForTwo(String a, String b, SparqlClient mySC) {
        String query = "SELECT ?lab WHERE\n"
                + "{\n"
                + "    {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?p <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?r <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?r ?p ?g.\n"
                + "    ?g <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + "}\n";
        return mySC.select(query);
    }

    private static Iterable<Map<String, String>> getRelatedForThree(String a, String b, String c, SparqlClient mySC) {
        String query = "SELECT ?lab WHERE\n"
                + "{\n"
                + "    {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\"@fr.\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\".\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\".\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\".\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\"@fr.\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\".\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\"@fr.\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\"@fr.\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\".\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + " UNION {?t <http://www.w3.org/2000/01/rdf-schema#label> \""+a+"\".\n"
                + "    ?q <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t.\n"
                + "    ?re <http://www.w3.org/2000/01/rdf-schema#label> \""+b+"\"@fr.\n"
                + "    ?p <http://www.w3.org/2000/01/rdf-schema#label> \""+c+"\"@fr.\n"
                + "    ?q ?re ?p.\n"
                + "    ?q <http://www.w3.org/2000/01/rdf-schema#label> ?lab.}\n"
                + "}\n";
        return mySC.select(query);
    }

    public static ArrayList<ArrayList<String>> transformWordsLists(ArrayList<ArrayList<String>> rawData){

        SparqlClient sparqlClient = new SparqlClient("localhost:3030/cinema");
        String query = "ASK WHERE { ?s ?p ?o }";
        boolean serverIsUp = sparqlClient.ask(query);

        ArrayList<ArrayList<String>> result = new ArrayList<>();

        //TODO : pondérer les mots rajoutés pour le calcul de l'accuracy sur un document (chaque syonyme d'un mot reçoit le coefficient 1/N, N étant le nombre de synonymes pour favoriser les labels recherchés sans synonymes)

        for (int i = 0; i < rawData.size(); i++){

            System.out.println("");
            System.out.println("* Editing request n°"+(i+1));
            System.out.println("");

            ArrayList<String> tempTable = rawData.get(i);
            ArrayList<String> tempSynonyms = new ArrayList<>();
            ArrayList<String> tempRelated = new ArrayList<>();

            System.out.println("Raw input data: " + tempTable);

            for (int j = 0; j < tempTable.size(); j++){
                String tempTerm = tempTable.get(j);

                System.out.println("Word : \""+tempTerm+"\"");

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
                        System.out.print("  Synonym word found : \""+r.get("lab")+"\"");

                    // ajout du synonyme en vérifiant u'il ne soit pas identique au mot recherché

                        if (!tempTerm.equals(r.get("lab"))){
                            System.out.println(" -- Added");
                            tempSynonyms.add(r.get("lab"));
                        } else {
                            System.out.println(" -- Not processed");
                        }
                    }
                }
            }

            if (serverIsUp) {
                Iterable<Map<String, String>> rs;
                switch(tempTable.size()) {
                    case 2 :
                        rs = getRelatedForTwo(tempTable.get(0), tempTable.get(1), sparqlClient);
                        for (Map<String, String> r : rs) {
                            System.out.println("  Related word found : \""+r.get("lab")+"\"");
                            tempRelated.add(r.get("lab"));
                        }
                        break;
                    case 3 :
                        rs = getRelatedForTwo(tempTable.get(0), tempTable.get(1), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForTwo(tempTable.get(0), tempTable.get(2), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForTwo(tempTable.get(1), tempTable.get(2), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForThree(tempTable.get(0), tempTable.get(1), tempTable.get(2), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForThree(tempTable.get(0), tempTable.get(2), tempTable.get(1), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForThree(tempTable.get(1), tempTable.get(0), tempTable.get(2), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForThree(tempTable.get(1), tempTable.get(2), tempTable.get(0), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForThree(tempTable.get(2), tempTable.get(0), tempTable.get(1), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        rs = getRelatedForThree(tempTable.get(2), tempTable.get(1), tempTable.get(0), sparqlClient);
                        for (Map<String, String> r : rs) {
                            if (!tempRelated.contains(r.get("lab")) && !tempTable.contains(r.get("lab"))){
                                System.out.println("  Related word found : \""+r.get("lab")+"\"");
                                tempRelated.add(r.get("lab"));
                            }
                        }
                        break;
                    default :
                }
            }

            System.out.println("Synonyms : " + tempSynonyms);
            System.out.println("Related words : " + tempRelated);

            tempSynonyms.addAll(tempTable);
            tempSynonyms.addAll(tempRelated);
            System.out.println("Onput data: " + tempSynonyms);

            result.add(tempSynonyms);
        }

        return result;
    }

}
