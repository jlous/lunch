import java.time.DayOfWeek;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.undertow.util.FileUtils;

class MainTest {

    public static final String UKENS_MENY = FileUtils.readFile(MainTest.class, "fromPdf.txt");

    @Test
    void mandag() {
        String bareDagens = Main.bareDagens(UKENS_MENY, DayOfWeek.MONDAY);
        Assertions.assertEquals(""
                + "2.\n"
                + "  Laks, spinatpesto og knuste søtpoteter\n"
                + "  Tomatsuppe med pølsebiter\n"
                + "  Blomkål, chili, ingefær, koriander og lime\n"
                + "1.\n"
                + "  Jerk chicken med søtpotet\n"
                + "  Kyllingkraft suppe med grønnsaker\n"
                + "  Paprika og fetaost"
            , bareDagens);
    }

    @Test
    void tirsdag() {
        String bareDagens = Main.bareDagens(UKENS_MENY, DayOfWeek.TUESDAY);
        System.out.println(bareDagens);
        Assertions.assertEquals(""
                + "2.\n"
                + "  Kylling overlår med teryaki, reven gulrot, hodekål og ris\n"
                + "  Grønn ertesuppe\n"
                + "  Vannmelon, fetost, mynte og chili\n"
                + "1.\n"
                + "  Gulasj\n"
                + "  Sopp\n"
                + "  Kylling og brie"
            , bareDagens);
    }

    @Test
    void onsdag() {
        String bareDagens = Main.bareDagens(UKENS_MENY, DayOfWeek.WEDNESDAY);
        System.out.println(bareDagens);
        Assertions.assertEquals(""
                + "2.\n"
                + "  Falaffel, pitabrød, tzatziki og fetaost\n"
                + "  Mais, chili og koriander\n"
                + "  Ananas og chili\n"
                + "1.\n"
                + "  Svinekotelett med fløtegratinert potet\n"
                + "  Aspargessuppe\n"
                + "  Bringebær og salami"
            , bareDagens);
    }

    @Test
    void torsdag() {
        String bareDagens = Main.bareDagens(UKENS_MENY, DayOfWeek.THURSDAY);
        System.out.println(bareDagens);
        Assertions.assertEquals(""
                + "2.\n"
                + "  Karbonade, karamelisert løk og potet\n"
                + "  Løksuppe\n"
                + "  Rødbeter, nøtter og chevreost\n"
                + "1.\n"
                + "  Laks med spinat og mango\n"
                + "  Fisk\n"
                + "  Frukt"
            , bareDagens);
    }

    @Test
    void fredag() {
        String bareDagens = Main.bareDagens(UKENS_MENY, DayOfWeek.FRIDAY);
        System.out.println(bareDagens);
        Assertions.assertEquals(""
                + "2.\n"
                + "  Calzone med ost og skinke\n"
                + "  Potetsuppe\n"
                + "  Aspargesbønner, fennikel og eple\n"
                + "1.\n"
                + "  Steak sandwich\n"
                + "  Blomkål\n"
                + "  Cæsarsalat"
            , bareDagens);
    }

    @Test
    void lørdag() {
        String bareDagens = Main.bareDagens(UKENS_MENY, DayOfWeek.SATURDAY);
        System.out.println(bareDagens);
        Assertions.assertEquals(""
                + "2.\n"
                + "  Stengt\n"
                + "1.\n"
                + "  Stengt"
            , bareDagens);
    }

}