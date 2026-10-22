# DEZSYS_GK71_FLOODMONITOR_REST

Hochwasser-Frühwarnsystem mit REST-Schnittstelle (JSON/XML), Simulation und Webclient.

## Startanleitung
Voraussetzungen: JDK 21, Gradle Wrapper
- Starten: `./gradlew bootRun` → http://localhost:8080
- Tests:   `./gradlew test`
- Swagger UI: http://localhost:8080/swagger-ui.html
- Konfiguration: `src/main/resources/application.properties` (simulation.interval, simulation.enabled)

## Architektur
Model (Datenklassen/Enums) → Repository (In-Memory) → Service (Simulation, Warnstufe, Statistik)
→ Controller (HTTP) → Exception Handling (@RestControllerAdvice) → Client (static/index.html)
Controller enthalten keine Geschäftslogik.

## Datenmodell
Station: id, name, river, location (lat/lon), normalWaterLevel, warningWaterLevel, criticalWaterLevel, isActive
Measurement: id, stationId, timestamp (ISO-8601), waterLevel, flowRate, rainfall, temperature, batteryLevel, status, warningLevel
Enums: WarningLevel (NORMAL/WARNING/CRITICAL/UNKNOWN), StationStatus (ONLINE/MAINTENANCE/OFFLINE)

## Simulationsregeln (Doku)
- Nur aktive Stationen erzeugen Messwerte; OFFLINE/MAINTENANCE-Ausfall → keine neuen Werte.
- Wasserstand basiert auf dem Vormesswert (Drift ±) und steigt tendenziell mit Niederschlag.
- Wasserstand nie negativ; Akku 0–100 %; Durchfluss korreliert mit Wasserstand.
- Akku < 10 % → Status MAINTENANCE.
- Warnstufe serverseitig: < warningWaterLevel = NORMAL; ≥ warningWaterLevel = WARNING; ≥ criticalWaterLevel = CRITICAL; keine Messung = UNKNOWN.

## Endpunkte
| Methode | Pfad | Parameter |
|---|---|---|
| GET | /api/v1/stations | river, isActive, warningLevel |
| GET | /api/v1/stations/{id} | – |
| GET | /api/v1/stations/{id}/measurements/latest | – |
| GET | /api/v1/stations/{id}/measurements | from, to, limit, warningLevel |
| GET | /api/v1/alerts | warningLevel, river |
| GET | /api/v1/statistics bzw. /api/v1/stations/{id}/statistics | – |

Content Negotiation: `Accept: application/json` (Default) oder `Accept: application/xml`; anderes Format → 406.

### Beispiele
curl -H "Accept: application/xml" http://localhost:8080/api/v1/stations/ST-001/measurements/latest
curl "http://localhost:8080/api/v1/stations/ST-001/measurements?from=2026-05-01T08:00:00&to=2026-05-01T12:00:00&limit=100"

## Fehlerbehandlung
Zentral via @RestControllerAdvice; einheitliches ErrorResponse (timestamp, status, error, message, path),
keine Stacktraces. 400 (ungültige Filter/Zeitraum/Limit), 404 (Station unbekannt), 406 (Format), 415, 500.

## Tests
`./gradlew test` – MockMvc-Tests: JSON/XML, existierende/unbekannte Station, Latest, Zeitraumfilter,
ungültiger Zeitraum, Warnstufen NORMAL/WARNING/CRITICAL, 406, max. Limit. Simulation in Tests deaktiviert
(`src/test/resources/application.properties`: simulation.enabled=false).

## Webclient
[Screenshot Tabelle einfügen] [Screenshot XML-Modus einfügen]

## Reflexion
- Testklasse lag anfangs in src/main/java → Compile-Fehler, da test-Dependencies fehlen; nach src/test/java verschoben.
- @EnableScheduling fehlte → Simulation lief nicht, leere Tabellen im Client.
- 406-Fehlerbody muss direkt geschrieben werden, da Message-Converter den Accept-Header ablehnen.
- jQuery parst XML-Antworten automatisch vor → dataType:'text' + manueller DOMParser.

## AI-Prompt-Dokumentation (Pflicht laut Angabe)
| Datum | Prompt (Kurzform) | Ergebnis/Übernahme |
|---|---|---|
| 13.10.2026 | "Setze die Angabe vollständig um" | Architektur-, Code-Vorschläge übernommen & angepasst |
| 13.10.2026 | "Erstelle Repository/Exception/Service-Klassen" | Code übernommen, Optional-Fix selbst eingespielt |
| ... | ... | ... |