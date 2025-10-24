CREATE TABLE IF NOT EXISTS persons (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(50),
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE IF NOT EXISTS clients (
    person_id BIGINT PRIMARY KEY,
    company_name VARCHAR(150),
    vat_number VARCHAR(50),
    FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS agents (
    person_id BIGINT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    region VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_update DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employees (
    person_id BIGINT PRIMARY KEY,
    job_title VARCHAR(100),
    FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS team_members (
    team_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (team_id, employee_id),
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS team_chat_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    team_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message NVARCHAR(1000) NOT NULL,
    sent_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES employees(person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS services (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12,2) NOT NULL,
    active BIT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    client_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(person_id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES agents(person_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paid BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE IF NOT EXISTS email_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body NVARCHAR(2000) NOT NULL,
    sent_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (employee_id) REFERENCES employees(person_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(person_id) ON DELETE CASCADE
);
