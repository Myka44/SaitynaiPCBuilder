# Azure App Service deployment

Deploy React and Spring Boot together to the existing Linux / Java 21 / Java SE Web App
`pc-builder-mykolas`. PostgreSQL runs separately on `pc-builder-mykolas-dbtest`.

## Build

Install Java 21 and Node.js 22.12 or newer. Stop the Vite development server before
running `npm ci` so Windows does not lock its native dependencies. Start Docker Desktop for the PostgreSQL
Testcontainers integration tests. Run from the repository root in PowerShell:

```powershell
Push-Location frontend
npm.cmd ci
npm.cmd run lint
npm.cmd test
npm.cmd run build
Pop-Location

Push-Location backend
.\mvnw.cmd -B clean verify -Pazure
Pop-Location
```

Stop if any command fails. The `azure` Maven profile includes `frontend/dist` under
`BOOT-INF/classes/static` and produces `backend/target/pc-builder.jar`.
Always build the frontend before packaging. Leave `VITE_API_BASE_URL` unset or set
it to `/api/v1` at frontend build time so browser requests use the same origin.
Local development remains `npm run dev` plus Spring Boot in separate terminals.

## Web App configuration

In Environment variables > App settings, configure:

| Setting | Value |
| --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://pc-builder-mykolas-dbtest.postgres.database.azure.com:5432/pc_builder?sslmode=require` |
| `DATABASE_USERNAME` | PostgreSQL administrator login |
| `DATABASE_PASSWORD` | PostgreSQL administrator password |
| `JWT_SECRET` | Base64 encoding of at least 32 cryptographically random bytes |
| `APP_ALLOWED_ORIGIN` | Exact HTTPS origin copied from the Web App default domain, without a trailing slash |
| `APP_COOKIE_SECURE` | `true` |
| `APP_COOKIE_SAME_SITE` | `Lax` |

Check the database hostname against the PostgreSQL Overview page. The database
`pc_builder` must already exist. Allow all current Web App outbound IPv4 addresses
in the PostgreSQL firewall; review these rules if the App Service plan changes.
Do not commit credentials. This lab seeds demonstration accounts documented in the
README, so use it only with demonstration data.

In Configuration > General settings, enable Always On for the B1 plan. Enable HTTPS
Only in the Web App settings. Keep the built-in Java SE startup configuration;
the deployment command uploads the executable JAR as `app.jar`.

## Deploy manually

Install Azure CLI on your computer. Authenticate and choose the Azure for Students
subscription before deploying; this command replaces the application on the existing
Web App and does not create new Azure resources:

```powershell
az login
az account list --output table
az account set --subscription "<your-student-subscription-id>"
az webapp deploy --resource-group rg-pc-builder --name pc-builder-mykolas --src-path backend/target/pc-builder.jar --type jar
```

Run these from the repository root. Deployment works with SCM basic authentication
disabled using a current Azure CLI and Microsoft Entra authentication.

## Verify

Open the exact HTTPS default domain from the Web App Overview. Check:

- `/actuator/health` reports `UP`.
- `/api/v1/components` returns HAL JSON and `/swagger-ui.html` opens Swagger.
- `/` opens React; opening `/catalog` directly and refreshing it also works.
- Sign-in, creating a configuration, refreshing the page, and sign-out work.
- A protected API request without a token returns `401`, not HTML.

Flyway creates tables and demonstration data on the first successful connection.
For startup failures, use App Service > Monitoring > Log stream. Do not paste
passwords, tokens, or complete secret environment-variable dumps into logs or chats.
