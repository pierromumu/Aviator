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

/**
 * Created by thomas on 09/12/16.
 */
public class Requests {

    public static ArrayList<ArrayList<String>> parse(Path file){
        ArrayList<ArrayList<String>> requests = new ArrayList<>();
        ArrayList<String> request;
        //For later
        ArrayList<String> questions = new ArrayList<>();

        String textContent = "";

        try {
            List<String> contentLines = Files.readAllLines(file, StandardCharsets.UTF_8);
            Iterator<String> iterator = contentLines.iterator();
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
            //Le premier élément ne contient aucune requête
            temp = requestsList[i];
            requestInside = temp.split("description");

            questions.add(requestInside[1].trim()); //Be careful about the question, things after it may be in it. (such as QX mots clés etc...)

            requestInside = requestInside[0].split(",");
            if (requestInside.length > 0){
                for (int j = 0; j < requestInside.length -1; j ++){
                    //-1 is there to remove the
                    request.add(requestInside[j].trim());
                }
            }
            requests.add((request));
        }

        return requests;
    }

}
