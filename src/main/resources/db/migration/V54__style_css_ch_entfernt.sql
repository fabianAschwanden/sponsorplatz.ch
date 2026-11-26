-- Der Style 'css-ch' (Theme-Override mit fremden Markentokens) ist entfernt.
-- Eine Installation, die ihn gewählt hatte, fällt auf 'default' zurück;
-- sonst zeigte die Admin-Seite einen Style, den es nicht mehr gibt.

UPDATE plattform_einstellungen
   SET aktiver_style = 'default'
 WHERE aktiver_style <> 'default';
