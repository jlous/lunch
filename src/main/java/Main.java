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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        Document ukeoversikt = Jsoup.connect(site + "/weekmenu/").get();
        Elements relativeLinks = ukeoversikt.select("a");
        return relativeLinks.stream()
            .map(element -> site + element.attr("href"))
            .map(Main::getInputStream)
            .map(Main::pdfToText)
            .map(Main::bareDagens)
            .collect(Collectors.joining("\n"));
    }

    private static InputStream getInputStream(String url) {
        try {
            return Unirest.get(url).asBinary().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private static String pdfToText(InputStream pdf) {
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
        String[] avsnitt = ukemeny.split("(Mandag|Tirsdag|Onsdag|Torsdag|Fredag)");
        String tittel = avsnitt[0].split("\n")[1];
        int dayOfWeek = Math.min(5, LocalDate.now().getDayOfWeek().getValue());
        return tittel + "\n" + prettyPrint(avsnitt[dayOfWeek]);
    }

    private static String prettyPrint(String dagens) {
        List<String> lines = Arrays.asList((dagens.trim().split("\\w+:")));
        Collections.reverse(lines);
        return lines.stream()
            .map(s -> s
                .replace("\n", "")
                .replaceAll(" kr ", "")
                .replaceAll("[0-9],?", "")
                .replace("/hg", "")
                .replace("/", "")
                .replace(";", "")
            )
            .filter(s -> !s.trim().isEmpty())
            .collect(Collectors.joining("\n"));
    }

}
