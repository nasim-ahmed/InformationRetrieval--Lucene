package ovgu.ir.infinity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;



public class Main {
    Indexer indexer;
    Searcher searcher;


    public static void main(String[] args) throws IOException {
        if (!args[0].equals("") && !args[1].equals("") && !args[2].equals("")) {
            Main main = new Main();

            String dataDirectory = args[0];
            String indexDirectory = args[1];
            String searchQuery = args[2];

            main.createIndex(dataDirectory, indexDirectory);

            try {
                main.search(searchQuery, indexDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            System.out.print("Invalid command line arguments. Must be as follows:\n");
            System.out.print("java -jar IR_P01.jar [path to document folder] [path to index folder] [query]\n" + "");
            return;
        }
    }


    private void createIndex(String docsPath, String indexPath) throws IOException{
        boolean isValidPath = checkDocDirectory(docsPath);
        if (isValidPath){
            indexer = new Indexer(indexPath);
            long startTime = System.currentTimeMillis();
            int numDocuments = indexer.createIndex(docsPath);
            long endTime = System.currentTimeMillis();
            indexer.close();
            System.out.println(numDocuments + " File indexed, time taken: "
                    +(endTime-startTime)+" ms");
        }
    }

    private boolean checkDocDirectory(String docsPath){
        final Path docDirectory = Paths.get(docsPath);

        if(!Files.isReadable(docDirectory)){
            System.out.println("Document directory '" + docDirectory.toAbsolutePath()+ "' does not exist");
            System.exit(1);
        }
        return true;
    }

    private void search(String searchQuery, String indexDirectoryPath) throws IOException, ParseException {
        searcher = new Searcher(indexDirectoryPath);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();

        System.out.println(hits.totalHits +
                " documents found. Time :" + (endTime - startTime));
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);

            String title = doc.get(LuceneConstants.TITLE);
            String path = doc.get(LuceneConstants.PATH_FIELD);
            String lastModified = doc.get("modified");


            double score = scoreDoc.score;

            System.out.println("Title: "+ title);
            System.out.println("Path: "+ path);
            System.out.println("Last Modified: "+ lastModified);
            System.out.println("Score: "+ score);

        }
    }

}
