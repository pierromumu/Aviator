package query;

import command.Command;
import tools.Tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Query {

    private List<String> result;
    private ArrayList<String> words;

    private String listOfDocText = "SELECT TITLE FROM documents WHERE ID IN ("; // ajouter );
    private String patternDocId = "SELECT DOC_ID FROM invertedindex WHERE WORD_ID IN (SELECT ID FROM vocabulary WHERE WORD = '"; // ajouter ')

    public Query(ArrayList<String> w){
        words = w;
    }

    public void execute(){
        result = new ArrayList<>();
        String sql = "";
        for(String w : words){
            w = Tools.cleanTerm(w);
            if(Tools.processTerm(w)){
                if(!(sql.equals(""))){
                    sql = sql + " INTERSECT ";
                }
                sql = sql + patternDocId + w + "')";
            }
        }
        sql = listOfDocText + sql + ");";

        ResultSet call = Command.executeQ(sql);

        try {
            while (call.next()){
                result.add(call.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Command.closeStmt();
    }

    public List<String> getResult(){
        return result;
    }

}
