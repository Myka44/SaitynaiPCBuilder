# PC Komplektatorius

Lietuviška kompiuterių komplektacijų kūrimo ir valdymo sistema su mockintais tiekėjų pasiūlymais. Projektą sudaro Spring Boot REST API, React SPA ir PostgreSQL duomenų bazė.

Diegimas į Azure App Service su bendru React ir Spring Boot JAR aprašytas [Azure instrukcijoje](docs/AZURE.md).

## Kas realizuota

- Trys pagrindiniai objektai ir hierarchija `Configuration M:N Component 1:N Offer`.
- Visi 15 bazinių CRUD/LIST metodų, trijų lygių hierarchinis endpointas ir komplektacijos suvestinė.
- Puslapiavimas, filtrai, ribojamas rūšiavimas ir HAL hypermedia nuorodos.
- `MEMBER`, `EDITOR`, `ADMIN` rolės bei anoniminė svečio prieiga.
- HS256 JWT access tokenas ir DB maiša saugomas rotuojamas opaque refresh tokenas.
- Vienodas RFC 9457 `ProblemDetail` klaidų formatas, validacija ir prasmingi HTTP kodai.
- Flyway schema, indeksai ir prasmingi demonstraciniai duomenys.
- Responsive React sąsaja, konstruktorius, modalai, toast, skeleton, filtrai ir rolėmis apsaugoti maršrutai.
- Swagger UI, Postman kolekcija, Testcontainers integraciniai ir Vitest UI testai.

## Reikalavimai

- Java 21 (IntelliJ gali naudoti atsisiųstą JDK 21).
- Docker Desktop.
- Node.js 22+ ir npm.

Maven atskirai diegti nereikia – `backend` kataloge yra Maven Wrapper.

## Paleidimas lokaliai

1. Nukopijuokite `.env.example` į `.env` ir pakeiskite `POSTGRES_PASSWORD`, `DATABASE_PASSWORD` bei `JWT_SECRET`. JWT paslaptis privalo būti standartiniu Base64 užkoduoti bent 32 atsitiktiniai baitai (ne paprastas tekstas). PowerShell sugeneruokite reikšmę:

   ```powershell
   $jwtKeyBytes = New-Object byte[] 32
   $jwtKeyGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $jwtKeyGenerator.GetBytes($jwtKeyBytes)
   $jwtKeyGenerator.Dispose()
   [Convert]::ToBase64String($jwtKeyBytes)
   ```

   Gautą reikšmę laikykite paslaptyje. Šiame projekte `application.yml` aiškiai importuoja `.env` iš darbinio arba jo tėvinio katalogo. Kintamuosius taip pat galima nustatyti IntelliJ Run Configuration arba terminalo aplinkoje. Azure naudokite Web App aplinkos kintamuosius. Docker Compose `.env` skaito atskirai.
2. Paleiskite PostgreSQL:

   Jeigu `5432` prievadą naudoja kita PostgreSQL instancija, `.env` faile nustatykite
   `POSTGRES_PORT=5433` ir `DATABASE_PORT=5433`. Docker konteinerio viduje DB ir toliau
   naudoja `5432`, o API iš kompiuterio jungiasi per `5433`.

   ```powershell
   docker compose up -d postgres
   ```

3. Kitame terminale paleiskite API:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

4. Dar kitame terminale paleiskite SPA:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

Atverkite `http://localhost:5173`. Swagger UI pasiekiamas per `http://localhost:8080/swagger-ui.html`, health – per `http://localhost:8080/actuator/health`.

Vite proxy lokaliai persiunčia `/api` užklausas į `localhost:8080`, todėl refresh slapukas veikia su `SameSite=Lax`.

## Demonstracinės paskyros

Visų paskyrų slaptažodis – `Password1`.

| Rolė | El. paštas |
|---|---|
| MEMBER | `narys@example.lt` |
| EDITOR | `redaktorius@example.lt` |
| ADMIN | `admin@example.lt` |

Tiekėjai „BalticPC Demo“, „TechTurgus Demo“ ir „Kompiuteka Demo“ yra išgalvoti. Svetainė nenaudoja scraping ir negauna realių kainų.

## IntelliJ IDEA

1. `File → Open` pasirinkite `backend/pom.xml` ir atverkite kaip projektą.
2. `Project SDK` nustatykite į JDK 21, o Maven runner – į projekto Wrapper.
3. `Run/Debug Configurations` sukurkite Spring Boot konfigūraciją klasei `lt.komplektatorius.PcBuilderApplication`.
4. Į environment variables įrašykite `.env.example` nurodytas reikšmes. Prieš startą turi veikti PostgreSQL konteineris.
5. Frontend galite paleisti IntelliJ npm konfigūracija, kurios command `run`, script `dev`, package.json – `frontend/package.json`.

## Testai ir build

```powershell
cd backend
.\mvnw.cmd verify

cd ..\frontend
npm run lint
npm test
npm run build
```

Backend integraciniai testai automatiškai sukuria izoliuotą PostgreSQL 17 Testcontainers konteinerį, todėl jiems turi veikti Docker.

## API santrauka

Visi API keliai prasideda `/api/v1`:

- `/configurations` – komplektacijų CRUD ir sąrašas;
- `/components` – komponentų CRUD ir sąrašas;
- `/components/{componentId}/offers`, `/offers/{id}` – pasiūlymų CRUD ir sąrašas;
- `/configurations/{configurationId}/components/{componentId}` – susiejimas ir atsiejimas;
- `/configurations/{configurationId}/components/{componentId}/offers` – trijų objektų hierarchinis metodas;
- `/configurations/{id}/summary` – kelių esybių kainos suvestinė;
- `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/me` – autentifikacija.

Pilną vykdomą metodų rinkinį rasite [Postman kolekcijoje](postman/PC-Builder.postman_collection.json).

## Render diegimas

`render.yaml` aprašo Docker backend, statinį frontend ir PostgreSQL Frankfurto regione. Kuriant Blueprint reikia įvesti dvi `sync: false` reikšmes:

- `APP_ALLOWED_ORIGIN` – pilnas frontend origin, pvz. `https://pc-builder-web.onrender.com`;
- `VITE_API_BASE_URL` – pilnas API prefiksas, pvz. `https://pc-builder-api.onrender.com/api/v1`.

`JWT_SECRET` Render nustatykite rankiniu būdu: naudokite aukščiau aprašytu būdu sugeneruotą Base64 paslaptį. Produkcijoje refresh slapukas nustatytas kaip `Secure; SameSite=None`.

## Projekto struktūra

```text
backend/   Spring Boot API, migracijos ir testai
frontend/  React/Vite SPA ir testai
postman/   API kolekcija bei lokali aplinka
compose.yaml
render.yaml
```
