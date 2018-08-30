import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

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

    static String dagensOversikt() throws IOException {
        String site = "https://fs4.m-eating.no";
        String landingUrl = site + "/weekmenu/";

        Document ukeoversikt = Jsoup.connect(landingUrl).get();
        Elements relativeLinks = ukeoversikt.select("a");
        return relativeLinks.stream()
            .map(resolveUrl(site))
            .map(Main::getInputStream)
            .map(Main::pdfToText)
            .map(bareDagens(LocalDate.now()))
            .collect(Collectors.joining("\n"));
    }

    private static Function<Element, String> resolveUrl(String site) {
        return element -> site + element.attr("href");
    }

    private static Function<String, String> bareDagens(LocalDate iDag) {
        return ukemeny -> bareDagens(ukemeny, iDag.getDayOfWeek());
    }

    static String bareDagens(String ukemeny, DayOfWeek ukedag) {
        String dagNavn = ukedag.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("no"));
        return dagens(ukemeny, dagNavn, "Kronen", "2.") + "\n"
            + dagens(ukemeny, dagNavn, "Bonusen", "1.");
    }

    private static String dagens(String ukemeny, String ukedag, String kantine, String tittel) {
        int startOfKantine = StringUtils.indexOfIgnoreCase(ukemeny, kantine);
        int startOfUkedag = StringUtils.indexOfIgnoreCase(ukemeny, ukedag, startOfKantine);
        String dagens;
        if (startOfKantine == -1 || startOfUkedag == -1) {
            dagens = "  Stengt";
        } else {
            int startOfDagens = ukemeny.indexOf("\n", startOfUkedag) + 1;
            int endOfDagens = ukemeny.indexOf("\n\n", startOfDagens);
            dagens = prettyPrint(ukemeny.substring(startOfDagens, endOfDagens));
        }
        return tittel + "\n" + dagens;
    }

    static String prettyPrint(String dagens) {
        List<String> retter = Arrays.asList((dagens.trim().split("\\w+:")));
        Collections.reverse(retter);
        return retter.stream()
            .map(s -> s
                .replaceAll("\\n", " ")
                .replaceAll("(?i)\\bkr(\\b|\\d).*", "")
                .replaceAll(" +", " ")
                .trim()
            )
            .filter(s -> !s.isEmpty())
            .map(StringUtils::capitalize)
            .map(s -> "  " + s)
            .collect(Collectors.joining("\n"));
    }

    static String pdfToText(InputStream pdf) {
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

    static InputStream getInputStream(String url) {
        try {
            return Unirest.get(url).asBinary().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

}
