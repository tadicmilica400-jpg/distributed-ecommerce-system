# Distributed E-Commerce System

A Java EE coursework project that implements a distributed online store using a central REST API, a command-line client, three independently deployed subsystems, JMS messaging, and separate MySQL persistence units.

## Architecture

```text
Command-line client
        |
        | HTTP / REST
        v
Central server (JAX-RS)
        |
        | JMS request/reply queues
        +----------------------+----------------------+
        v                      v                      v
User subsystem          Sales subsystem       Transaction subsystem
users and cities        catalog and carts     orders and payments
        |                      |                      |
        +----------------------+----------------------+
                               |
                         JPA / MySQL
```

## Features

- User and city management, account balances, addresses, and role-aware operations
- Product categories, catalog items, prices, discounts, carts, and wish lists
- Checkout, order creation, order items, and payment transaction records
- REST endpoints implemented with JAX-RS
- Asynchronous request-reply communication between the central server and subsystems through JMS queues
- Persistence with JPA entities and separate MySQL databases for users, sales, and transactions
- Interactive Java command-line client for calling the REST API

## Technology stack

- Java and Java EE
- JAX-RS REST services
- JMS / OpenMQ message queues
- JPA with EclipseLink
- MySQL
- GlassFish
- Apache Ant and NetBeans project structure

## Project structure

- `CentralniServer` - REST API and JMS request routing
- `Klijent` - interactive command-line client
- `podsistem1_korisnici` - users, cities, balances, addresses, and roles
- `podsistem2_prodaja` - categories, products, carts, discounts, and wish lists
- `podsistem3_transakcije` - orders, order items, payments, and transaction history

## Local setup

1. Create the MySQL databases `is1_korisnici`, `is1_prodaja`, and `is1_transakcije`.
2. Review the JDBC URLs and credentials in each subsystem's `persistence.xml` for your local environment.
3. Configure OpenMQ queues for each subsystem and its reply queue.
4. Set `JMS_USER` and `JMS_PASSWORD` if your broker does not use the local GlassFish defaults.
5. Deploy `CentralniServer` to GlassFish and start the three subsystem applications.
6. Run `Klijent` and use the numbered menu to call the API.

## Security note

Local passwords, IDE-private settings, compiled artifacts, and deployment packages are intentionally excluded from this repository. Use environment-specific credentials and never commit real secrets.

## Context

This project was developed as university coursework in Information Systems. It demonstrates service decomposition, synchronous HTTP APIs, asynchronous messaging, relational data modeling, and multi-database transaction workflows.

