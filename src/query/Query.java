package query;

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

/**
 * Created by thomas on 25/11/16.
 */
public class Query {

    /*public HashMap<String, Integer> query(String query_word){

    }*/

    //Fonction principale
    public void execute(){
        Path file = Paths.get("./files/corpus-utf8/requetes.html");
        String textContent = "";
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
    }

}
