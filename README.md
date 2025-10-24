# ManagementAgent

Suite composta da un server REST e da un client JavaFX per la gestione di agenti commerciali.

## Requisiti
- Java 17+
- Maven 3.9+
- Un'istanza SQL Server raggiungibile (parametri configurabili in `server/src/main/resources/application.properties`)
- Eseguire lo script `server/src/main/resources/schema.sql` per creare la tabella `agents`

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

## Avvio del client

```bash
cd client
mvn javafx:run -Dmanagement.agent.api=http://localhost:7070
```

L'applicazione JavaFX consente di visualizzare, creare, aggiornare ed eliminare agenti comunicando con il server tramite chiamate REST asincrone.

## Pattern utilizzati
- **Singleton**: `DatabaseConnectionManager` gestisce un'unica istanza di `DataSource` condivisa.
- **Factory Method**: `AgentFactory` incapsula la creazione di oggetti `Agent` dalla richiesta REST.
- **DAO (Data Access Object)**: `AgentDAO` e `SqlServerAgentDAO` isolano l'accesso al database SQL Server.
- **Observer**: `AgentEventPublisher` e `LoggingAgentListener` permettono di reagire agli eventi di creazione/aggiornamento/eliminazione degli agenti.

## Multithreading
Sia il server (`AgentService`) sia il client (`AgentApiClient`) utilizzano `ExecutorService` e `CompletableFuture` per gestire in modo non bloccante le operazioni I/O mantenendo l'interfaccia reattiva e scalabile.
