import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class Main {

    public String stemTerm (String term) {
        PorterStemmer stemmer = new PorterStemmer();
        return stemmer.stem(term);
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

        // test d'affichage
        System.out.println("Texte pre traitement" + sampleDocument.text());

        String[] words = sampleDocument.text().split((" "));

        for (String word : words) {



        }



    }
}
