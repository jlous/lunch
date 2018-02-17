import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(final String[] args) {
        Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(Main::handleRequest)
            .build()
            .start();
    }

    private static void handleRequest(HttpServerExchange exchange) throws IOException {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
        exchange.getResponseSender().send(dagensOversikt());
    }

    private static String dagensOversikt() throws IOException {
        String site = "https://fs4.m-eating.no";
        String startUrl = site + "/weekmenu/";
        Document ukeoversikt = Jsoup.connect(site + "/weekmenu/").get();
        Elements links = ukeoversikt.select("a");
        return links.stream()
            .map(element -> site + element.attr("href"))
            .map(Main::getInputStream)
            .map(Main::pdfToText)
            .map(Main::bareDagens)
            .map(Main::prettyPrint)
            .collect(Collectors.joining());
    }

    private static InputStream getInputStream(String url) {
        try {
            return Unirest.get(url).asBinary().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static String pdfToText(InputStream pdf) {
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

    private static String bareDagens(String ukemeny) {
        //TODO: kantinenavn
        int dayOfWeek = Math.min(5, LocalDate.now().getDayOfWeek().getValue());
        String[] dager = ukemeny.split("(Mandag|Tirsdag|Onsdag|Torsdag|Fredag)");
        return dager[dayOfWeek];
    }

    private static String prettyPrint(String dagens) {
        //TODO: rekkefølge
        //TODO: -pris, type
        return dagens;
    }

}
