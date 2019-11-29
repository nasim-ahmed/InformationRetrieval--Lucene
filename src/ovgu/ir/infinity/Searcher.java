package ovgu.ir.infinity;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.search.similarities.ClassicSimilarity;

public class Searcher {
    IndexSearcher indexSearcher;
    IndexReader reader;

    public Searcher(String indexDirectoryPath)
            throws IOException {
        Directory indexDirectory =
                FSDirectory.open(Paths.get(indexDirectoryPath));
        reader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(reader);
        ClassicSimilarity similarity = new ClassicSimilarity();
        indexSearcher.setSimilarity(similarity);
       /* queryParser = new QueryParser(LuceneConstants.CONTENTS,
                new StandardAnalyzer());*/

    }


    public Document getDocument(ScoreDoc scoreDoc)
            throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    public TopDocs searchMultipleFields(String searchQuery) throws ParseException, IOException {
        String[] fields = {LuceneConstants.TITLE, LuceneConstants.CONTENTS};

        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        Query query = parser.parse(searchQuery);

        reader.close();

        return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
    }


}
