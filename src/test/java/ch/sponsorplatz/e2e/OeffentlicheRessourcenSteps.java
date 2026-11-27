package ch.sponsorplatz.e2e;

import java.util.List;
import java.util.regex.Pattern;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OeffentlicheRessourcenSteps {

    private static final Pattern EXTERNE_HTML_RESSOURCE =
            Pattern.compile("(?i)<(?:link|script)\\b[^>]+(?:href|src)\\s*=\\s*[\"']\\s*(?:https?:)?//");
    private static final Pattern EXTERNE_CSS_RESSOURCE =
            Pattern.compile("(?i)(?:@import\\s+(?:url\\()?\\s*[\"']?|url\\(\\s*[\"']?)(?:https?:)?//");
    private static final Pattern EXTERNE_JS_RESSOURCE =
            Pattern.compile("(?i)(?:fetch\\s*\\(\\s*|import\\s*\\(\\s*|src\\s*=\\s*[\"'])\\s*(?:https?:)?//");
    private static final List<String> SCHRIFTEN = List.of(
            "barlow-400-latin.woff2",
            "barlow-400-latin-ext.woff2",
            "barlow-400-italic-latin.woff2",
            "barlow-400-italic-latin-ext.woff2",
            "barlow-600-latin.woff2",
            "barlow-600-latin-ext.woff2",
            "barlow-700-latin.woff2",
            "barlow-700-latin-ext.woff2",
            "barlow-condensed-600-latin.woff2",
            "barlow-condensed-600-latin-ext.woff2",
            "barlow-condensed-700-latin.woff2",
            "barlow-condensed-700-latin-ext.woff2");

    @Autowired
    private MockMvc mockMvc;

    private String html;
    private String css;
    private String skript;

    @Angenommen("die öffentliche Testseite ist mit dem Fragment gerendert")
    public void testseiteIstGerendert() throws Exception {
        html = mockMvc.perform(get("/__test/oeffentlich"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Wenn("ich Stylesheet, Skript und Schriften ohne Anmeldung lade")
    public void ressourcenOhneAnmeldungLaden() throws Exception {
        assertThat(html).contains("/css/hallenboden.css", "/js/farbschema.js");

        MvcResult cssAntwort = mockMvc.perform(get("/css/hallenboden.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/css")))
                .andReturn();
        css = cssAntwort.getResponse().getContentAsString();

        MvcResult skriptAntwort = mockMvc.perform(get("/js/farbschema.js"))
                .andExpect(status().isOk())
                .andReturn();
        skript = skriptAntwort.getResponse().getContentAsString();

        for (String schrift : SCHRIFTEN) {
            mockMvc.perform(get("/fonts/barlow/{schrift}", schrift))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("font/woff2")))
                    .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());
        }
    }

    @Dann("enthalten die Ressourcen keine fremden Adressen")
    public void ressourcenSindLokal() {
        assertThat(EXTERNE_HTML_RESSOURCE.matcher(html).find()).isFalse();
        assertThat(EXTERNE_CSS_RESSOURCE.matcher(css).find()).isFalse();
        assertThat(EXTERNE_JS_RESSOURCE.matcher(skript).find()).isFalse();
    }
}
