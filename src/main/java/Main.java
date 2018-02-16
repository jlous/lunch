import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class Main {

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(new HttpHandler() {
                public void handleRequest(final HttpServerExchange exchange) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(parsePdf(inputStream("Ukesmeny uke 7 2018 Bonusen 1 etg.pdf")));
                }
            }).build();
        server.start();
    }

    public static String parsePdf(InputStream input) {
        try {
            PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(input));
            parser.parse();
            try (COSDocument cosDoc = parser.getDocument(); PDDocument pdDoc = new PDDocument(cosDoc)) {
                return new PDFTextStripper().getText(pdDoc);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private static InputStream inputStream(String filename) {
        return Main.class.getResourceAsStream(filename);
    }
}
