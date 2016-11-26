import index.*;
import command.*;
import query.*;

import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {

        Command.connect();

        //Index.createIndex();

        /*
            EXEMPLE DE REQUETE : on trouve l'ensemble des documents contenant les termes "omar" et "intouchables"
            (la liste est non ordonnée pour le moment car le nombre d'occurrences de chaque mot n'est pas retenu)
        */
        ArrayList<String> param = new ArrayList<>();
        param.add("omar");
        param.add("intouchables");
        Query q = new Query(param);
        q.execute();
        System.out.println(q.getResult());
    }
}
