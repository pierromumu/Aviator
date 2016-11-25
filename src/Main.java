import com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.sql.*;

public class Main {

    public static String stemTerm(String term) {
        FrenchStemmer stemmer = new FrenchStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public static void createTables(Connection c){

        try {
            Statement stmt = c.createStatement();

            // table des documents
            String sql =    "CREATE TABLE IF NOT EXISTS documents"+
                            " (ID serial PRIMARY KEY NOT NULL,"+
                            " TITLE text NOT NULL);";
            stmt.executeUpdate(sql);

            // table contenant les identifiants des mots, le texte ainsi que le nombre d'occurences dans le corpus pris en entier
            sql =           "CREATE TABLE IF NOT EXISTS vocabulary"+
                            " (ID serial PRIMARY KEY NOT NULL,"+
                            " WORD text NOT NULL,"+
                            " FREQ integer NOT NULL DEFAULT '0');";
            stmt.executeUpdate(sql);

            // index inversé
            sql =           "CREATE TABLE IF NOT EXISTS invertedindex "+
                            " (WORD_ID serial NOT NULL,"+
                            " DOC_ID serial NOT NULL,"+
                            " OCC integer NOT NULL,"+
                            " primary key (WORD_ID, DOC_ID),"+
                            " foreign key (WORD_ID) references vocabulary(ID),"+
                            " foreign key (DOC_ID) references documents(ID));";
            stmt.executeUpdate(sql);

            stmt.close();

            System.out.println("Tables created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void dropTables(Connection c){

        try {
            Statement stmt = c.createStatement();
            String sql = "DROP TABLE documents, invertedindex, vocabulary;";
            stmt.executeUpdate(sql);
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertDocument(Connection c, String docTitle, int docId){

        try {
            Statement stmt = c.createStatement();
            String sql = "INSERT INTO documents (ID, TITLE) VALUES ('"+docId+"', '"+docTitle+"');";
            stmt.executeUpdate(sql);
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertWord(Connection c, String word, int docId, int nb_occ){

        try {
            Statement stmt = c.createStatement();

            // ajout du mot au vocabulaire si besoin et incrémentation des occurrences

            String sql = "SELECT EXISTS(SELECT * FROM vocabulary WHERE WORD='"+word+"');";
            ResultSet result = stmt.executeQuery(sql);
            result.next();

            if (!(result.getBoolean("exists"))){
                sql = "INSERT INTO vocabulary(WORD, FREQ) VALUES ('"+word+"', '"+nb_occ+"');";
                stmt.executeUpdate(sql);
            }

            sql = "UPDATE vocabulary SET FREQ = FREQ + "+nb_occ+" WHERE WORD='"+word+"';";
            stmt.executeUpdate(sql);

            // mise à jour de l'index inversé

            sql = "SELECT ID FROM vocabulary WHERE WORD='"+word+"'";
            result = stmt.executeQuery(sql);
            int word_id = 0;
            if(result.next())
                word_id = result.getInt(1);

            sql = "SELECT EXISTS(SELECT * FROM invertedindex WHERE WORD_ID='"+word_id+"' AND DOC_ID='"+docId+"');";
            result = stmt.executeQuery(sql);
            result.next();

            if (!(result.getBoolean("exists"))){
                sql = "INSERT INTO invertedindex (WORD_ID, DOC_ID, OCC) VALUES ('"+word_id+"', '"+docId+"', '"+nb_occ+"');";
                stmt.executeUpdate(sql);
            } else {
                sql = "UPDATE invertedindex SET OCC = OCC +"+nb_occ+" WHERE WORD_ID='"+word_id+"' AND DOC_ID='"+docId+"';";
                stmt.executeUpdate(sql);
            }

            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertVoc(Connection c, HashMap<String, Integer[]> vocabulary) {

        String sql = "INSERT INTO vocabulary(ID, WORD, FREQ) VALUES ";

        for (String key : vocabulary.keySet()){
            sql = sql+("("+vocabulary.get(key)[0]+", '"+key+"', "+vocabulary.get(key)[1]+"), ");
        }

        sql = sql.substring(0, sql.lastIndexOf(","));
        sql = sql+";";
        Statement stmt;
        try {
            stmt = c.createStatement();

            stmt.executeUpdate(sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void insertIndex(Connection c, HashMap<Integer, HashMap<Integer, Integer>> word_list){

        String doc_sql = "INSERT INTO documents (ID, TITLE) VALUES ";
        String words_sql = "INSERT INTO invertedindex (WORD_ID, DOC_ID, OCC) VALUES ";

        for (int key : word_list.keySet()){
            doc_sql = doc_sql+"("+key+", 'D"+key+"'), ";
            for (int word : word_list.get(key).keySet()) {
                words_sql = words_sql + "(" + word + ", " + key + ", " + word_list.get(key).get(word) + ") ,";
            }
        }

        doc_sql = doc_sql.substring(0, doc_sql.lastIndexOf(","));
        doc_sql = doc_sql+";";
        words_sql = words_sql.substring(0, words_sql.lastIndexOf(","));
        words_sql = words_sql+";";
        Statement stmt;
        try {
            stmt = c.createStatement();

            stmt.executeUpdate(doc_sql);
            stmt.executeUpdate(words_sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* */

    public static void main(String[] args) {

        try {

            Class.forName("org.postgresql.Driver");
            // dbname, username & password = aviator
            Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/aviator", "aviator", "aviator");
            System.out.println("Database opened successfully.");

            dropTables(c);

            // création des tables dans la base de données
            createTables(c);

            HashMap<String, Integer[]> vocabulary = new HashMap<>();
            //First element: Id, second element: value
            int voc_id = 0;


            //HashMap<Integer, Integer> word_in_doc = new HashMap<Integer, Integer>();

            HashMap<Integer, HashMap<Integer, Integer>> doc_voc = new HashMap<>();
            //First element: ID, second: freq
            //HashMap<String, Integer> word_list = new HashMap<String, Integer>();


            System.out.println("Starting Data gathering");

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

                //System.out.println("Texte original : " + currentDocument.text());

                //insertDocument(c, documentName, i);


                //Cette hashmap contient les valeurs avant l'intégration dans la bdd, objectif: économiser les e/s de la bdd
                HashMap<Integer, Integer> current_doc = new HashMap<>();

                for (String word : words) {
                    word = word.toLowerCase();
                    //System.out.println(word);
                    int word_id;
                    int word_occ;
                    word = stemTerm(word.replaceAll("[\\W]",""));
                    if (vocabulary.containsKey(word)){
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

                    if (current_doc.containsKey(word_id)){
                        current_doc.put(word_id, current_doc.get(word_id) + 1);
                    } else {
                        current_doc.put(word_id, 1);
                    }

                    doc_voc.put(i, current_doc);

                    //System.out.println(word);
                }




                System.out.println("Document "+documentName+".html successfully processed.");
            }

            System.out.println("All data gathered, commencing index.");

            insertVoc(c, vocabulary);
            System.out.println("Vocabulary saved");

            insertIndex(c, doc_voc);

            System.out.println("Index Finished");

            c.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage());
            System.exit(0);
        }
    }
}
