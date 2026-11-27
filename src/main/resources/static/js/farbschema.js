(() => {
    const wurzel = document.documentElement;
    const speicherSchluessel = "sponsorplatz-farbschema";
    const systemSchema = () => window.matchMedia("(prefers-color-scheme: dark)").matches ? "dunkel" : "hell";

    const gespeichertesSchema = () => {
        try {
            return window.localStorage.getItem(speicherSchluessel);
        } catch (fehler) {
            if (fehler instanceof DOMException) return null;
            throw fehler;
        }
    };

    const anwenden = (schema) => {
        wurzel.dataset.farbschema = schema;
    };

    const speichern = (schema) => {
        try {
            window.localStorage.setItem(speicherSchluessel, schema);
        } catch (fehler) {
            if (!(fehler instanceof DOMException)) throw fehler;
        }
    };

    const gespeichertes = gespeichertesSchema();
    anwenden(gespeichertes === "hell" || gespeichertes === "dunkel" ? gespeichertes : systemSchema());

    const knopfAktivieren = () => {
        document.querySelectorAll("[data-farbschema-knopf]").forEach((knopf) => {
            knopf.addEventListener("click", () => {
                const schema = wurzel.dataset.farbschema === "dunkel" ? "hell" : "dunkel";
                anwenden(schema);
                speichern(schema);
            });
        });
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", knopfAktivieren, { once: true });
    } else {
        knopfAktivieren();
    }
})();
