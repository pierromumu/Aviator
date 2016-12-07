import index.*;
import command.*;
import query.*;

import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {

        Command.connect();

        //Index.createIndex();

        // EXEMPLE DE REQUETE
        ArrayList<String> param = new ArrayList<>();
        param.add("personnes");
        param.add("Intouchables");
        Query q1 = null;
        try {
            q1 = new Query(param);
        } catch (Exception e) {
            System.out.println("Error creating the query : check your input terms !");
        }
        q1.execute();
    }
}
