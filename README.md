# ManagementAgent

Suite composta da un server REST e da un client JavaFX per la gestione di agenti commerciali.

## Requisiti
- Java 17+
- Maven 3.9+
- Un'istanza SQL Server raggiungibile (parametri configurabili in `server/src/main/resources/application.properties`)
- Eseguire lo script `server/src/main/resources/schema.sql` per creare l'intero schema, comprensivo delle nuove tabelle `employees`, `teams`, `team_members`, `team_chat_messages` ed `email_messages`

## Avvio del server

```bash
cd server
mvn package
java -jar target/management-agent-server-1.0.0-jar-with-dependencies.jar
```

Il server espone API REST su `http://localhost:7070` con le seguenti rotte principali:

- `GET /agents` – elenco di tutti gli agenti
- `GET /agents/{id}` – recupera un agente specifico
- `POST /agents` – crea un nuovo agente
- `PUT /agents/{id}` – aggiorna un agente esistente
- `DELETE /agents/{id}` – elimina un agente
- `GET /collaboration/teams` – restituisce i team aziendali con i relativi membri
- `GET /collaboration/employees` – restituisce l'anagrafica dei dipendenti
- `GET /collaboration/clients` – restituisce l'anagrafica clienti con i recapiti email
- `GET /collaboration/teams/{teamId}/messages` – recupera la chat condivisa del team
- `POST /collaboration/teams/{teamId}/messages` – crea un nuovo messaggio nella chat di team (solo membri del team)
- `POST /communications/email` – invia una email da un dipendente ad un cliente e registra l'interazione

## Avvio del client

```bash
cd client
mvn javafx:run -Dmanagement.agent.api=http://localhost:7070
```

L'applicazione JavaFX ora offre tre aree:

- Gestione agenti con funzioni CRUD asincrone
- Chat interna tra colleghi appartenenti allo stesso team, con aggiornamento dei messaggi dal server
- Modulo di invio email: selezione del dipendente mittente e del cliente destinatario e consegna della mail tramite SMTP configurabile

Il client sfrutta nuove chiamate REST per popolare le combobox e registrare le interazioni.

## Configurazione autenticazione Windows

Per utilizzare l'autenticazione integrata di Windows con SQL Server configurare:

```properties
db.url=jdbc:sqlserver://<host>:1433;databaseName=AgentDB;encrypt=false;integratedSecurity=true;authenticationScheme=NativeAuthentication
db.authentication=windows
db.sqljdbc.auth.dll=C:\\percorso\\sqljdbc_auth.dll  # opzionale, richiesto sui sistemi Windows
```

Quando `db.authentication` è impostato su `windows` non è necessario specificare `db.username` e `db.password`.

## Configurazione SMTP

Aggiornare `mail.smtp.*` in `application.properties` con il server aziendale. Se è richiesta autenticazione impostare username e password; il servizio utilizza TLS e l'invio avviene in modo asincrono.

## Pattern utilizzati
- **Singleton**: `DatabaseConnectionManager` gestisce un'unica istanza di `DataSource` condivisa.
- **Factory Method**: `AgentFactory` incapsula la creazione di oggetti `Agent` dalla richiesta REST.
- **DAO (Data Access Object)**: `AgentDAO`/`SqlServerAgentDAO` e `CollaborationDAO`/`SqlServerCollaborationDAO` isolano l'accesso al database.
- **Observer**: `AgentEventPublisher` e `LoggingAgentListener` permettono di reagire agli eventi di creazione/aggiornamento/eliminazione degli agenti.
- **Service Layer**: `TeamService` e `MailService` orchestrano logica asincrona riusabile per chat ed email.

## Multithreading
Sia il server (`AgentService`, `TeamService`, `MailService`) sia il client (`AgentApiClient`) utilizzano `ExecutorService` e `CompletableFuture` per gestire in modo non bloccante le operazioni I/O mantenendo l'interfaccia reattiva e scalabile.
