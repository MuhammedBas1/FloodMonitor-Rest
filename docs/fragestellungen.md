# Fragestellungen (Punkt 12 der Angabe)

## 1. Was sind die Hauptmerkmale von JSON?
- Textbasiertes, leichtgewichtiges Datenformat (JavaScript Object Notation).
- Aufbau aus Objekten (Key-Value-Paaren) und Arrays; minimale Syntax (`{}`, `[]`, `:` , `,`).
- Für Menschen und Maschinen leicht les- und schreibbar, in nahezu jeder Sprache parsbar.
- Kein Schema-Zwang, wenig Metadaten → kompakte Übertragung; De-facto-Standard für Web-APIs.
- **Quellverweis:** Standard-Serialisierung in diesem Projekt über Jackson (`MappingJackson2HttpMessageConverter`, enthalten in `spring-boot-starter-web`, siehe `build.gradle`); Beispielklasse `model/Measurement.java`; Feinjustierung in `src/main/resources/application.properties` (`spring.jackson.serialization.*`).

## 2. Was sind die Hauptmerkmale von XML?
- Auszeichnungssprache (Extensible Markup Language) mit hierarchischem Element-Baum (Öffnungs-/Schluss-Tags).
- Strikte Wohlgeformtheit; unterstützt Attribute, Namespaces und Schema-Validierung (XSD).
- Selbstbeschreibend, dadurch aber ausführlicher (höherer Overhead) als JSON.
- **Quellverweis:** XML-Fähigkeit über Dependency `com.fasterxml.jackson.dataformat:jackson-dataformat-xml` in `build.gradle`; Wurzel- und Listenelemente werden in `model/MeasurementList.java` und `model/StationList.java` über `@JacksonXmlRootElement` bzw. `@JacksonXmlElementWrapper(useWrapping = false)` definiert.

## 3. Wie unterscheidet die Applikation, ob JSON oder XML zurückgegeben wird?
Per **Content Negotiation**: Der Client sendet einen `Accept`-Header (`application/json` oder `application/xml`). Spring MVC vergleicht ihn mit den `produces`-Medientypen der Controller-Methode und wählt den passenden `HttpMessageConverter` (Jackson JSON bzw. Jackson XML). Ohne `Accept`-Header greift die Default-Reihenfolge → JSON. Ein unsupported Format (z. B. `application/pdf`) führt zu `HttpMediaTypeNotAcceptableException` → HTTP 406.
- **Quellverweis:** `controller/StationController.java` u. a. (`@GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})`); Fehlerfall `exception/GlobalExceptionHandler.java`, Methode `handleNotAcceptable(...)`; Converter-Registrierung durch `jackson-dataformat-xml` in `build.gradle`.

## 4. Was zeichnet ein REST-Interface aus und warum ist es so verbreitet?
- Architekturstil (kein Protokoll) mit: Client-Server-Trennung, **Zustandslosigkeit** (jeder Request trägt alle Infos), **einheitlicher Schnittstelle** (Ressourcen-URIs + Standard-HTTP-Methoden), Cacheability und Layered System.
- Verbreitung, weil es auf allgegenwärtigem HTTP aufsetzt (keine Zusatzprotokolle), plattform- und sprachunabhängig ist, durch Zustandslosigkeit horizontal skaliert und Systeme lose koppelt → einfache Integration, Tooling und Caching.
- **Quellverweis:** Ressourcen-URIs `/api/v1/...` in `controller/StationController.java`, `MeasurementController.java`, `AlertController.java`, `StatisticsController.java`; zustandslose Requests (In-Memory-Repositories `repository/StationRepository.java`, `MeasurementRepository.java`); einheitliche Statuscodes via `exception/GlobalExceptionHandler.java`.

## 5. Unterschied zwischen GET, POST, PUT und PATCH?
- **GET**: Ressource lesen; *safe* und idempotent, keine Zustandsänderung.
- **POST**: Neue Ressource erzeugen; nicht idempotent; Erfolg typisch `201 Created` (+ `Location`-Header).
- **PUT**: Ressource unter definierter URI **vollständig ersetzen**; idempotent.
- **PATCH**: **Teilupdate** einer Ressource; Idempotenz nicht garantiert.
- **Quellverweis:** Die API ist bewusst leseorientiert und setzt ausschließlich `@GetMapping` ein (z. B. `MeasurementController.getLatestMeasurement(...)`); warum ein CREATE niemals GET sein darf, zeigt Frage 8.

## 6. Neue Request-Methode/Pfad: Welche Quellcode-Stellen muss ich verändern?
1. **Controller**: neue Methode mit Mapping-Annotation (z. B. `@PostMapping`), Pfad, Parameter-Binding, `produces` – `controller/`-Paket.
2. **Service**: Geschäftslogik/Validierung – `service/`-Paket (z. B. `StationService`, `MeasurementService`).
3. **Repository**: Datenzugriff, falls neue Abfragen nötig – `repository/`-Paket.
4. **Model/DTO**: Request-/Response-Klassen – `model/`-Paket.
5. Optional: `GlobalExceptionHandler` (neue Fehlerbilder), OpenAPI-Annotationen (`@Operation`, `@ApiResponse`), Tests (`src/test/java/...`), Webclient (`src/main/resources/static/index.html`).
- **Quellverweis (Beispiel aus diesem Projekt):** Der Statistik-Endpunkt entstand exakt so: `model/Statistics.java` → `service/StatisticsService.java` → `controller/StatisticsController.java`.

## 7. Kann man OpenAPI und Swagger vergleichen?
Ja – sie gehören zusammen, sind aber nicht identisch:
- **OpenAPI** ist die **Spezifikation** (maschinell lesbares Beschreibungsformat, YAML/JSON; hervorgegangen aus der „Swagger Specification", seit 20