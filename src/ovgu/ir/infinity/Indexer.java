package ovgu.ir.infinity;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

public class Indexer {
    private IndexWriter writer;


    public Indexer(String indexDirectoryPath) throws IOException {

        boolean isCreated = true;


        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        if(isCreated){
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        }else{
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }
        writer = new IndexWriter(indexDirectory, iwc);
    }


    public void close() throws IOException{
        writer.close();
    }

    private Document getDocument(File file, long lastModified) throws IOException {
        Document doc = new Document();

        org.jsoup.nodes.Document parsedDoc = HTMLParser.parseHTML(file);
        String parsedContents = parsedDoc.body().text();
        String stemmedContents = Stemmer.stemming(parsedContents);


        Field filePathField = new StringField(LuceneConstants.PATH_FIELD, file.toString(), Field.Store.YES);
        Field modifiedField = new LongPoint("modified", lastModified);
        Field titleField = new TextField(LuceneConstants.TITLE, parsedDoc.title(), Field.Store.YES);
        Field contentsField = new TextField(LuceneConstants.CONTENTS, stemmedContents, Field.Store.NO);

        doc.add(filePathField);
        doc.add(modifiedField);
        doc.add(titleField);
        doc.add(contentsField);


        return doc;
    }

    private void indexFile(File file, long lastModified) throws IOException {
        System.out.println("Indexing "+file.getAbsolutePath());


        Document document = getDocument(file, lastModified);
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            System.out.println("Adding " + file);
            writer.addDocument(document);

        } else {
            System.out.println("Updating " + file);
            writer.updateDocument(new Term(LuceneConstants.PATH_FIELD, file.toString()), document);
        }
    }



    public int createIndex(String dataDirPath) throws IOException {
        File directory = new File(dataDirPath);

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    if(!file.isDirectory()
                            && !file.isHidden()
                            && file.exists()
                            && file.canRead()
                    ){

                        indexFile(file, file.lastModified());
                    }

                } else if (file.isDirectory()) {
                    createIndex(file.getAbsolutePath());
                }
            }
        return writer.numRamDocs();
    }






}
