import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class Main {

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(Main::handleRequest).build();
        server.start();
    }

    private static void handleRequest(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
        exchange.getResponseSender().send(dagensMenyer());
    }

    private static String dagensMenyer() {
        return menyLenker(getAsString("http://www.google.com/"))
            .map(Main::getAsInputStream)
            .map(Main::toText)
            .map(Main::bareDagens)
            .collect(Collectors.joining());
    }

    private static String bareDagens(String ukemeny) {
        return null;
    }

    private static InputStream getAsInputStream(URL url) {
        return null;
    }

    private static Stream<URL> menyLenker(String html) {
        return null;
    }

    private static String getAsString(String url) {
        try {
            return Unirest.get(url).asString().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toText(InputStream pdf) {
        try {
            PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(pdf));
            parser.parse();
            try (COSDocument cosDoc = parser.getDocument(); PDDocument pdDoc = new PDDocument(cosDoc)) {
                return new PDFTextStripper().getText(pdDoc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fetch() throws UnirestException {
        Unirest.get("http://www.google.com/").asBinary();
        return "";
    }

    private static InputStream inputStream(String filename) {
        return Main.class.getResourceAsStream(filename);
    }
}
