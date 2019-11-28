package ovgu.ir.infinity;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Indexer {
    private IndexWriter writer;
    boolean isCreated = false;

    public Indexer(String indexDirectoryPath) throws IOException {

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


    public void close() throws CorruptIndexException, IOException{
        writer.close();
    }

    private void indexFile(Path file, long lastModified) throws IOException {
        Document document = getDocument(file, lastModified);
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            System.out.println("Adding " + file);
            writer.addDocument(document);
            isCreated = true;
        } else {
            System.out.println("Updating " + file);
            writer.updateDocument(new Term("path", file.toString()), document);
            isCreated = false;
        }
    }

    private Document getDocument(Path file, long lastModified) throws IOException {

            Document document = new Document();

            org.jsoup.nodes.Document parsedDoc = HTMLParser.parseHTML(file);
            String parsedContents = parsedDoc.body().text();
            String stemmedContents = Stemmer.stemming(parsedContents);


            Field filePathField = new StringField(LuceneConstants.PATH_FIELD, file.toString(), Field.Store.YES);
            Field modifiedField = new LongPoint(LuceneConstants.LAST_MODIFIED, lastModified);
            Field titleField = new TextField(LuceneConstants.TITLE, parsedDoc.title(), Field.Store.YES);
            Field contentsField = new TextField(LuceneConstants.CONTENTS, stemmedContents, Field.Store.NO);

            document.add(filePathField);
            document.add(modifiedField);
            document.add(titleField);
            document.add(contentsField);

            return document;

    }

    public void createIndex(String dataDirPath) throws IOException {
        final Path docDir = Paths.get(dataDirPath);
        if (Files.isDirectory(docDir)) {
            Files.walkFileTree(docDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        // Recurse the directory tree
                          indexFile(file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // Ignore files that cannot be read
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
             indexFile(docDir, Files.getLastModifiedTime(docDir).toMillis());
        }

    }

}
