package query;

//import com.sun.org.apache.xpath.internal.operations.String;
import command.Command;
import tools.Tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Query {

    private HashMap<String, Integer> result;
    private ArrayList<String> words;

    //private String listOfDocText = "SELECT TITLE FROM documents WHERE ID IN ("; // ajouter );
    private String patternDocId = "SELECT DOC_ID FROM invertedindex WHERE WORD_ID IN (SELECT ID FROM vocabulary WHERE WORD = '"; // ajouter ')

    private String docList = "SELECT * FROM invertedindex WHERE WORD_ID IN (SELECT ID FROM vocabulary WHERE WORD = '"; // ajouter ')
    private String finalList = "SELECT documents.TITLE, docs.OCC, docs.WORD_ID FROM documents JOIN ( SELECT * FROM (";

    //SELECT documents.TITLE, invertedindex.OCC FROM invertedindex JOIN documents ON invertedindex.DOC_ID = documents.ID WHERE invertedindex.WORD_ID IN (..)

    //SELECT * FROM invertedindex WHERE WORD_ID IN (SELECT ID FROM vocabulary WHERE WORD = 'omar') UNION SELECT * FROM invertedindex WHERE WORD_ID IN (SELECT ID FROM vocabulary WHERE WORD = 'intouch') WHERE DOC_ID IN (SELECT DOC_ID

    public Query(ArrayList<String> w){
        words = w;
    }

    public void execute(){
        result = new HashMap<>();
        String sql = "";
        String usql = "";
        for(String w : words){
            w = Tools.cleanTerm(w);
            if(Tools.processTerm(w)){
                if(!(sql.equals(""))){
                    sql = sql + " INTERSECT ";
                    usql = usql + " UNION ";
                }
                sql = sql + patternDocId + w + "')"; //patternDocId
                usql = usql + docList + w + "')";
            }
        }
        sql = finalList + usql + ") as occurences WHERE DOC_ID IN (" + sql + ")) docs ON docs.DOC_ID = documents.ID;"; //listOfDocText ==> Renvoie liste des docs


        ResultSet call = Command.executeQ(sql);

        try {
            while (call.next()){
                //result.add(call.getString(1));

                /************* ESPACE ALGO ************/
                //Ideally, result should be of the form: <Document ID, accuracy value>

                if (result.containsKey(call.getString(1))){
                    result.put(call.getString(1), call.getInt(2) + result.get(call.getString(1)));
                } else {
                    result.put(call.getString(1), call.getInt(2));
                }

                /**************** FIN ESPACE ALGO ************/

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Command.closeStmt();
    }

    public HashMap<String, Integer> getResult(){
        return result;
    }

    public void displayResult(){
        if (!(result.isEmpty())){
            //Get the max, put it into a "done" table, and gets next value
            String[] document = result.keySet().toArray(new String[0]);
            //Order in documents; values in result.

        } else {
            System.out.println("There are no results for this query or no query has been made so far.");
        }
    }

}
