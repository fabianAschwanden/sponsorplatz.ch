package ch.sponsorplatz.home;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Import({OeffentlicheTestController.class, OeffentlicheTestSecurityConfig.class})
class OeffentlicheSeitenRessourcenTest {

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

    @Test
    void fragmentStylesheetUndSkriptNutzenNurLokaleAdressen() throws Exception {
        String html = mockMvc.perform(get("/__test/oeffentlich"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String css = new ClassPathResource("static/css/hallenboden.css")
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        String skript = new ClassPathResource("static/js/farbschema.js")
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(html).contains("/css/hallenboden.css", "/js/farbschema.js");
        assertThat(EXTERNE_HTML_RESSOURCE.matcher(html).find()).isFalse();
        assertThat(EXTERNE_CSS_RESSOURCE.matcher(css).find()).isFalse();
        assertThat(EXTERNE_JS_RESSOURCE.matcher(skript).find()).isFalse();

        mockMvc.perform(get("/css/hallenboden.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/css")));
        mockMvc.perform(get("/js/farbschema.js"))
                .andExpect(status().isOk());
    }

    @Test
    void schriftdateienSindOhneAnmeldungAlsWoff2Erreichbar() throws Exception {
        for (String schrift : SCHRIFTEN) {
            mockMvc.perform(get("/fonts/barlow/{schrift}", schrift))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("font/woff2")))
                    .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());
        }
    }
}
