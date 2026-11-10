# Distributed E-Commerce System

## Project overview

A Java EE coursework implementation of an online-store backend divided into a central REST service, a command-line client and three messaging-connected subsystems with separate persistence units.

## Academic context

Developed for university coursework in Information Systems. The project demonstrates service decomposition, synchronous HTTP interfaces, JMS request/reply communication and relational persistence. It is a learning project, not a production e-commerce or payment platform.

## Architecture

```text
Command-line client
        |
        | HTTP / JAX-RS
        v
Central server
        |
        | JMS request/reply queues with request identifiers
        +----------------------+----------------------+
        v                      v                      v
User subsystem          Sales subsystem       Transaction subsystem
users and cities        catalogue and carts   orders and payments
        |                      |                      |
        v                      v                      v
   MySQL database         MySQL database         MySQL database
```

The central resources send text messages to subsystem queues and wait for correlated replies with a five-second timeout. Each subsystem listens on its own queue and uses JPA entities/services for persistence.

## Technologies

- Java and the legacy `javax.*` Java EE APIs
- JAX-RS, EJB and JMS/OpenMQ
- JPA with EclipseLink
- MySQL
- GlassFish, Apache Ant and NetBeans project metadata

## Main functionality

- Users, cities, account balances, addresses and roles
- Categories, catalogue items, prices, discounts, carts and wish lists
- Orders, order items, payments and transaction history
- REST resources that route operations to the responsible subsystem
- Interactive Java client for invoking the REST API

## Repository structure

| Directory | Responsibility |
| --- | --- |
| `CentralniServer` | JAX-RS endpoints and JMS request routing |
| `Klijent` | Interactive command-line REST client |
| `podsistem1_korisnici` | Users, cities, balances, addresses and roles |
| `podsistem2_prodaja` | Categories, products, carts, discounts and wish lists |
| `podsistem3_transakcije` | Orders, line items, payments and history |

## Local configuration

The source expects MySQL databases named `is1_korisnici`, `is1_prodaja` and `is1_transakcije`, an OpenMQ broker at `localhost:7676`, and queues named `podsistem1Queue`, `podsistem1ReplyQueue`, `podsistem2Queue`, `podsistem2ReplyQueue`, `podsistem3Queue` and `podsistem3ReplyQueue`.

JMS credentials can be supplied with `JMS_USER` and `JMS_PASSWORD`; the local defaults are `admin` and an empty password. The committed persistence files likewise use the local-development MySQL user `root` with an empty password. These are not real credentials, but they are unsafe defaults outside an isolated local environment and should be replaced before use.

## Reproducibility status

The repository is **not currently reproducible from a clean machine without additional setup information**:

- database DDL or migration scripts are not included, while JPA schema generation is set to `none`;
- GlassFish/OpenMQ resources and queues must be created manually;
- the compatible GlassFish, Java, MySQL and connector versions are not pinned;
- no automated tests or end-to-end setup script are included;
- Ant/GlassFish deployment was not executed during this audit.

The source and NetBeans/Ant project files are present, but a successful clean deployment has not been independently verified.

## Limitations and future improvements

- Request/reply calls block while waiting for JMS responses and return text-based error messages.
- Cross-subsystem checkout is not documented as an atomic distributed transaction; failure recovery and idempotency need explicit treatment.
- Authentication, authorisation, transport security and production secret management are outside the demonstrated scope.
- Useful improvements are versioned database migrations, containerised infrastructure, typed message contracts, integration tests and documented transaction/failure semantics.


