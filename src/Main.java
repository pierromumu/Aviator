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

    public static void insertWord(Connection c, String word, int docId){

        try {
            Statement stmt = c.createStatement();

            // ajout du mot au vocabulaire si besoin et incrémentation des occurrences

            String sql = "SELECT EXISTS(SELECT * FROM vocabulary WHERE WORD='"+word+"');";
            ResultSet result = stmt.executeQuery(sql);
            result.next();

            if (!(result.getBoolean("exists"))){
                sql = "INSERT INTO vocabulary(WORD) VALUES ('"+word+"');";
                stmt.executeUpdate(sql);
            }

            sql = "UPDATE vocabulary SET FREQ = FREQ + 1 WHERE WORD='"+word+"';";
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
                sql = "INSERT INTO invertedindex (WORD_ID, DOC_ID, OCC) VALUES ('"+word_id+"', '"+docId+"', '1');";
                stmt.executeUpdate(sql);
            } else {
                sql = "UPDATE invertedindex SET OCC = OCC +1 WHERE WORD_ID='"+word_id+"' AND DOC_ID='"+docId+"';";
                stmt.executeUpdate(sql);
            }

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

                insertDocument(c, documentName, i);
                for (String word : words) {
                    //System.out.println(word);
                    word = stemTerm(word.replaceAll("[\\W]",""));
                    insertWord(c, word, i);
                    //System.out.println(word);
                }


                System.out.println("Document "+documentName+".html successfully processed.");
            }

            c.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage());
            System.exit(0);
        }
    }
}
