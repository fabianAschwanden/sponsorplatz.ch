package ch.sponsorplatz.shared.config;

import ch.sponsorplatz.shared.crypto.HmacSha256;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class FormularDrosselungService {

    private static final Duration FENSTER = Duration.ofHours(1);
    private static final long LIMIT_PRO_ADRESSE = 10;
    private static final long LIMIT_PRO_FORMULAR = 60;

    private final FormularDrosselungRepository eintraege;
    private final FormularDrosselungSperreRepository sperren;
    private final String schluessel;
    private final Clock clock;

    @Autowired
    public FormularDrosselungService(FormularDrosselungRepository eintraege,
            FormularDrosselungSperreRepository sperren,
            @Value("${sponsorplatz.drosselung.schluessel}") String schluessel, Clock clock) {
        if (schluessel == null || schluessel.length() < 32) {
            throw new IllegalStateException(
                    "Der Drosselungs-Schlüssel fehlt oder ist kürzer als 32 Zeichen; prüfe die Secret-Datei drossel_schluessel.");
        }
        this.eintraege = eintraege;
        this.sperren = sperren;
        this.schluessel = schluessel;
        this.clock = clock;
    }

    @Transactional
    public boolean erfasseEingabe(Formular formular, String clientAdresse) {
        sperren.findeUndSperre(formular)
                .orElseThrow(() -> new IllegalStateException("Formular-Drosselung ist nicht initialisiert."));

        Instant jetzt = clock.instant();
        String pruefwert = pruefwert(clientAdresse);
        Instant fensterBeginn = jetzt.minus(FENSTER);
        long proAdresse = eintraege.countByFormularAndPruefwertAndErstelltAmAfter(
                formular, pruefwert, fensterBeginn);
        long insgesamt = eintraege.countByFormularAndErstelltAmAfter(formular, fensterBeginn);
        if (proAdresse >= LIMIT_PRO_ADRESSE || insgesamt >= LIMIT_PRO_FORMULAR) {
            return false;
        }

        eintraege.save(new FormularDrosselungEintrag(formular, pruefwert, jetzt));
        return true;
    }

    @Transactional
    public int loescheAeltereAls(Instant zeitpunkt) {
        return eintraege.deleteByErstelltAmBefore(zeitpunkt);
    }

    String pruefwert(String adresse) {
        byte[] eingabe = normalisiereAdresse(adresse);
        return HmacSha256.berechne(schluessel.getBytes(StandardCharsets.UTF_8), eingabe);
    }

    private byte[] normalisiereAdresse(String adresse) {
        if (adresse == null || adresse.isBlank()) {
            throw new IllegalArgumentException("Die Client-Adresse fehlt.");
        }
        String wert = adresse.trim();
        if (wert.contains(":")) {
            return ipv6Praefix(wert);
        }
        return ipv4Bytes(wert);
    }

    private byte[] ipv6Praefix(String adresse) {
        if (!adresse.matches("[0-9a-fA-F:.]+")) {
            throw new IllegalArgumentException("Die Client-Adresse ist ungültig.");
        }
        try {
            InetAddress ip = InetAddress.getByName(adresse);
            if (!(ip instanceof Inet6Address)) {
                throw new IllegalArgumentException("Die Client-Adresse ist ungültig.");
            }
            byte[] praefix = new byte[9];
            praefix[0] = 6;
            System.arraycopy(ip.getAddress(), 0, praefix, 1, 8);
            return praefix;
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Die Client-Adresse ist ungültig.", ex);
        }
    }

    private byte[] ipv4Bytes(String adresse) {
        String[] teile = adresse.split("\\.", -1);
        if (teile.length != 4) {
            throw new IllegalArgumentException("Die Client-Adresse ist ungültig.");
        }
        byte[] adresseBytes = new byte[5];
        adresseBytes[0] = 4;
        try {
            for (int i = 0; i < 4; i++) {
                if (teile[i].isEmpty() || teile[i].length() > 3) {
                    throw new NumberFormatException();
                }
                int oktett = Integer.parseInt(teile[i]);
                if (oktett > 255) {
                    throw new NumberFormatException();
                }
                adresseBytes[i + 1] = (byte) oktett;
            }
            return adresseBytes;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Die Client-Adresse ist ungültig.", ex);
        }
    }
}
