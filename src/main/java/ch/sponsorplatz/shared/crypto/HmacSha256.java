package ch.sponsorplatz.shared.crypto;

import java.security.GeneralSecurityException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HmacSha256 {

    private HmacSha256() {
    }

    public static String berechne(byte[] schluessel, byte[] wert) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(schluessel, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(wert));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Der HMAC-Prüfwert konnte nicht berechnet werden.", ex);
        }
    }
}
