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

    public static String stemTerm (String term) {
        FrenchStemmer stemmer = new FrenchStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public static void createDBs (Connection c){
        Statement stmt = null;

        try {
            stmt = c.createStatement();
            //Table contenant les id des mots et les mots eux-même
            String sql = "CREATE TABLE IF NOT EXISTS words " +
                    "(ID serial PRIMARY KEY     NOT NULL," +
                    " WORD           TEXT    NOT NULL)";
            stmt.executeUpdate(sql);

            //Table contenant les id des fichiers et les id des mots contenus
            sql = "CREATE TABLE IF NOT EXISTS filesToWords " +
                    "(ID INT unique     NOT NULL," +
                    " WORD_ID  INT    NOT NULL," +
                    "OCCURRENCES INT NOT NULL," +
                    "primary key (ID, WORD_ID)," +
                    "foreign key(WORD_ID) references words(ID));";
            stmt.executeUpdate(sql);

            stmt.close();

            System.out.println("Table created successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void insertWord (Connection c, String word, int fileId){
        Statement stmt = null;

        try {

            stmt = c.createStatement();
            //select exists(select * from words where id=2);

            //Ajout du mot à la table contenant tous les mots
            String sql = "select exists(select * from words where word='"+word+"');";
            ResultSet result = stmt.executeQuery(sql);
            result.next();

            if (!(result.getBoolean("exists"))){


                sql = "INSERT INTO words (word) values ('"+word+"');";
                stmt.executeUpdate(sql);
            }

            sql = "select id from words where word='"+word+"'";
            result = stmt.executeQuery(sql);

            result.next();
            int word_id = result.getInt("id");

            //Ajout du mot pour dans la table des occurences
            sql = "select exists(select * from filesToWords where word_id='"+word_id+"' AND id='"+fileId+"');";
            result = stmt.executeQuery(sql);

            result.next();
            if (!(result.getBoolean("exists"))){
                sql = "INSERT INTO filesToWords (id, word_id, OCCURRENCES) values ('"+fileId+"', '"+word_id+"', '1');";
                stmt.executeUpdate(sql);
            } else {
                sql = "UPDATE filesToWords SET occurences = OCCURRENCES +1 WHERE word_id='"+word_id+"' AND id='"+fileId+"';";
                stmt.executeUpdate(sql);
            }



            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String sampleContent = "";
        Path sampleFile = Paths.get("./files/Google.html");
        try {
            List<String> sampleLines = Files.readAllLines(sampleFile, StandardCharsets.UTF_8);
            Iterator<String> sampleIterator = sampleLines.iterator();
            while (sampleIterator.hasNext()) {
                sampleContent += sampleIterator.next();
            }

            sampleContent = sampleLines.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document sampleDocument = Jsoup.parse(sampleContent);

        Connection c = null;

        //********************************************************************************
        // @Pierre: Pour créer la base de données, l'utilisateur via postgresql
        //https://www.postgresql.org/message-id/4D958A35.8030501@hogranch.com
        //********************************************************************************
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/aviator",
                            "aviator", "aviator");
            System.out.println("Opened database successfully");

            //Création des db
            createDBs(c);

            String[] words = sampleDocument.text().split((" "));


            // test d'affichage
            //System.out.println("Texte pre traitement" + sampleDocument.text());

            for (String word : words) {

                //System.out.println(word);
                word = stemTerm(word);
                insertWord(c, word, 0); //fileId à 0 pour le test
                //System.out.println(word);

            }


            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }





    }
}
