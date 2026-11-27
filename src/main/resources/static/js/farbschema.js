(() => {
    const wurzel = document.documentElement;
    const schluessel = "sponsorplatz-theme";
    const schema = ["auto", "light", "dark"];
    const bezeichnung = { auto: "Automatisch", light: "Hell", dark: "Dunkel" };
    let auswahl = "auto";

    try {
        const gespeichert = window.localStorage.getItem(schluessel);
        if (gespeichert === "light" || gespeichert === "dark") auswahl = gespeichert;
    } catch (fehler) {
        if (!(fehler instanceof DOMException)) throw fehler;
    }

    if (auswahl === "auto") wurzel.removeAttribute("data-theme");
    else wurzel.setAttribute("data-theme", auswahl);

    const anwenden = (knopf, status) => {
        if (auswahl === "auto") wurzel.removeAttribute("data-theme");
        else wurzel.setAttribute("data-theme", auswahl);
        knopf.dataset.state = auswahl;
        knopf.title = "Farbschema: " + bezeichnung[auswahl];
        status.textContent = bezeichnung[auswahl];
    };

    const initialisieren = () => {
        const knopf = document.getElementById("theme-toggle");
        const status = document.getElementById("theme-state");
        if (!knopf || !status) return;

        anwenden(knopf, status);
        knopf.addEventListener("click", () => {
            auswahl = schema[(schema.indexOf(auswahl) + 1) % schema.length];
            try {
                if (auswahl === "auto") window.localStorage.removeItem(schluessel);
                else window.localStorage.setItem(schluessel, auswahl);
            } catch (fehler) {
                if (!(fehler instanceof DOMException)) throw fehler;
            }
            anwenden(knopf, status);
        });
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initialisieren, { once: true });
    } else {
        initialisieren();
    }
})();
