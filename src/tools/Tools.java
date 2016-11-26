package tools;


import org.tartarus.snowball.ext.FrenchStemmer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.CharArraySet;

public class Tools {

    private static CharArraySet stopWords = FrenchAnalyzer.getDefaultStopSet();

    private static String stemTerm(String term){

        FrenchStemmer stemmer = new FrenchStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();

        return stemmer.getCurrent();
    }

    public static String cleanTerm(String term){

        String cleanedTerm;
        cleanedTerm = term.toLowerCase();

        return stemTerm(cleanedTerm.replaceAll("[\\W]",""));
    }

    public static boolean processTerm(String term){
        if(stopWords.contains(term))
            return false;
        else
            return true;
    }
}
