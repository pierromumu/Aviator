package requests;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Requests {

    // parsage du fichier contenant les requêtes

    /*STRUCTURE:
    *
    * DOCUMENT
    *  | LISTE DE MOTS
    *  |  |  MOT [0]
    *  |  |  POIDS [1]
    *
    * */

    public static ArrayList<ArrayList<ArrayList<String>>> parse(Path file){

        ArrayList<ArrayList<ArrayList<String>>> requests = new ArrayList<>();
        ArrayList<ArrayList<String>> request;
        ArrayList<String> word;
        ArrayList<String> questions = new ArrayList<>();

        String textContent = "";

        try {
            List<String> contentLines = Files.readAllLines(file, StandardCharsets.UTF_8);
            textContent = contentLines.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Document currentDocument = Jsoup.parse(textContent);

        String[] requestsList = currentDocument.text().split(("mots clés"));
        String[] requestInside;
        String temp;

        for (int i = 1; i < requestsList.length; i++){
            request = new ArrayList<>();

            // le premier élément ne contient aucune requête
            temp = requestsList[i];
            requestInside = temp.split("description");

            questions.add(requestInside[1].trim());

            requestInside = requestInside[0].split(",");
            if (requestInside.length > 0){
                for (int j = 0; j < requestInside.length -1; j ++){
                    word = new ArrayList<>();
                    word.add(requestInside[j].trim());
                    word.add("1.0");
                    request.add(word);
                }
            }
            requests.add(request);

        }

        return requests;
    }
}
