# ACME Commerce Services

A small multi-module Spring Boot platform made of three microservices and a shared library.

| Module                 | Port | Description                                                        |
|------------------------|------|--------------------------------------------------------------------|
| `common`               |  –   | Shared DTOs, `ApiError` + `@RestControllerAdvice`, correlation-id servlet filter |
| `order-service`        | 8081 | Accepts orders, reserves stock via `inventory-service`, notifies via `notification-service` (JPA/H2, springdoc OpenAPI) |
| `inventory-service`    | 8082 | Stock levels and reservations per SKU (JPA/H2, caching, method security) |
| `notification-service` | 8083 | Renders templated notifications, in-memory store                   |

## Stack

- Java 21
- Spring Boot 3.5.x (Spring Framework 6, Spring Security 6, Hibernate 6)
- `jakarta.*` APIs (persistence, validation, servlet, annotation)
- springdoc-openapi 2.x for OpenAPI docs on `order-service`
- Maven multi-module build with the Maven wrapper

## Build and test

```bash
./mvnw -B verify
```

Run a single service:

```bash
./mvnw -pl inventory-service spring-boot:run
./mvnw -pl notification-service spring-boot:run
./mvnw -pl order-service spring-boot:run
```

## Try it

```bash
# inventory is seeded on startup
curl -s localhost:8082/api/inventory | jq

# place an order (order-service calls inventory + notifications)
curl -s -u customer:customer -H 'Content-Type: application/json' \
  -d '{"customerEmail":"jane@example.com","currency":"USD","lines":[{"sku":"SKU-MUG-BLUE","quantity":2}]}' \
  localhost:8081/api/orders | jq

# swagger ui
open http://localhost:8081/swagger-ui.html
```

Credentials (in-memory, per service):

| Service              | User / password                 | Roles              |
|----------------------|---------------------------------|--------------------|
| order-service        | `customer` / `customer`, `admin` / `admin` | CUSTOMER, ADMIN |
| inventory-service    | `order-service` / `order-service`, `warehouse` / `warehouse` | SERVICE, ADMIN |
| notification-service | `order-service` / `order-service`, `support` / `support` | PUBLISHER, SUPPORT |
