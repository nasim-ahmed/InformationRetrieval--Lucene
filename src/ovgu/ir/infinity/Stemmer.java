package ovgu.ir.infinity;

import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer {

    public static String stemming(String parsedContents){
        String stemmedString = "";

        PorterStemmer stemmer = new PorterStemmer();

        String [] wordArray = parsedContents.split("\\s+");

        for(String word: wordArray){
            stemmer.setCurrent(word);

            stemmer.stem();

            String wordStemmed = stemmer.getCurrent();

            if(stemmedString.equalsIgnoreCase("")){
                stemmedString = wordStemmed;
            }else{
                stemmedString += " ";
                stemmedString += wordStemmed;
            }
        }
        return stemmedString;

    }

}
