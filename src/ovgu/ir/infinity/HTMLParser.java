package ovgu.ir.infinity;

import org.jsoup.Jsoup;

import java.io.*;
import java.nio.file.Path;
import java.util.Scanner;

public class HTMLParser {
    public static org.jsoup.nodes.Document parseHTML(File file)  {
        Scanner scanner = null;
        String rawContents = null;

        try {
            scanner = new Scanner( new File(file.toString()) );
            rawContents = scanner.useDelimiter("\\A").next();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            scanner.close();
        }

        // Parse the raw html using Jsoup
        org.jsoup.nodes.Document document = Jsoup.parse(rawContents);
        return document;
    }

}
