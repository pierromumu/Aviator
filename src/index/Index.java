package index;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import command.*;
import tools.*;


public class Index {

    private static void createTables(){

        String sql;

        // table des documents
        sql =   "CREATE TABLE IF NOT EXISTS documents"+
                " (ID serial PRIMARY KEY NOT NULL,"+
                " TITLE text NOT NULL);";
        Command.executeU(sql);

        // table contenant les identifiants des mots, le texte ainsi que le nombre d'occurences dans le corpus pris en entier
        sql =   "CREATE TABLE IF NOT EXISTS vocabulary"+
                " (ID serial PRIMARY KEY NOT NULL,"+
                " WORD text NOT NULL,"+
                " FREQ integer NOT NULL DEFAULT '0');";
        Command.executeU(sql);

        // index inversé
        sql =   "CREATE TABLE IF NOT EXISTS invertedindex "+
                " (WORD_ID serial NOT NULL,"+
                " DOC_ID serial NOT NULL,"+
                " OCC integer NOT NULL,"+
                " primary key (WORD_ID, DOC_ID),"+
                " foreign key (WORD_ID) references vocabulary(ID),"+
                " foreign key (DOC_ID) references documents(ID));";
        Command.executeU(sql);

        System.out.println("    Tables created successfully.");
    }

    private static void dropTables(){

        String sql = "DROP TABLE documents, invertedindex, vocabulary;";
        Command.executeU(sql);
    }

    private static void insertVoc(HashMap<String, Integer[]> vocabulary) {

        String sql = "INSERT INTO vocabulary(ID, WORD, FREQ) VALUES ";

        for(String key : vocabulary.keySet()){
            sql = sql+("("+vocabulary.get(key)[0]+", '"+key+"', "+vocabulary.get(key)[1]+"), ");
        }

        sql = sql.substring(0, sql.lastIndexOf(","));
        sql = sql+";";

        Command.executeU(sql);
    }

    private static void insertIndex(HashMap<Integer, HashMap<Integer, Integer>> word_list){

        String doc_sql = "INSERT INTO documents (ID, TITLE) VALUES ";
        String words_sql = "INSERT INTO invertedindex (WORD_ID, DOC_ID, OCC) VALUES ";

        for(int key : word_list.keySet()){

            doc_sql = doc_sql+"("+key+", 'D"+key+"'), ";

            for(int word : word_list.get(key).keySet()) {
                words_sql = words_sql + "(" + word + ", " + key + ", " + word_list.get(key).get(word) + ") ,";
            }
        }

        doc_sql = doc_sql.substring(0, doc_sql.lastIndexOf(","));
        doc_sql = doc_sql+";";
        words_sql = words_sql.substring(0, words_sql.lastIndexOf(","));
        words_sql = words_sql+";";

        Command.executeU(doc_sql);
        Command.executeU(words_sql);
    }

    public static void createIndex(){

        System.out.println("Creating the index...");

        // création des tables dans la base de données après reset
        dropTables();
        createTables();

        int voc_id = 0;

        // first element: id, second element: value
        //HashMap<Integer, Integer> word_in_doc = new HashMap<Integer, Integer>();

        // first element: id, second element: freq
        //HashMap<String, Integer> word_list = new HashMap<String, Integer>();

        HashMap<String, Integer[]> vocabulary = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Integer>> doc_voc = new HashMap<>();

        System.out.println("    Gathering data...");

        for(int i=1;i<=138;i++) {

            String documentName="D"+i;
            String textContent = "";
            Path file = Paths.get("./files/corpus-utf8/"+documentName+".html");

            try {
                List<String> contentLines = Files.readAllLines(file, StandardCharsets.UTF_8);
                Iterator<String> iterator = contentLines.iterator();
                while (iterator.hasNext()) {
                    textContent += iterator.next();
                }
                textContent = contentLines.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            Document currentDocument = Jsoup.parse(textContent);

            String[] words = currentDocument.text().split((" "));

            // hashmap contenant les valeurs avant intégration dans la BD (objectif : économiser les e/s en accès)
            HashMap<Integer, Integer> current_doc = new HashMap<>();

            for (String word : words) {

                word = Tools.cleanTerm(word);

                if(Tools.processTerm(word)){

                    int word_id;
                    int word_occ;

                    if(vocabulary.containsKey(word)){

                        word_id = vocabulary.get(word)[0];
                        word_occ = vocabulary.get(word)[1] +1;
                        Integer[] current_word = {word_id, word_occ};
                        vocabulary.put(word, current_word);

                    } else {

                        Integer[] current_word = {voc_id, 1};
                        word_id = voc_id;
                        voc_id++;
                        vocabulary.put(word, current_word);
                    }

                    if(current_doc.containsKey(word_id)){
                        current_doc.put(word_id, current_doc.get(word_id) + 1);
                    } else {
                        current_doc.put(word_id, 1);
                    }

                    doc_voc.put(i, current_doc);

                    //System.out.println(word);
                }
            }

            System.out.println("        Document "+documentName+".html successfully processed.");
        }

        System.out.println("    All data gathered.");

        insertVoc(vocabulary);
        System.out.println("    Vocabulary saved.");

        insertIndex(doc_voc);
        System.out.println("    Index Finished !");
    }
}
