# boost-springboot

Backend Engineer

## Database

The application is configured to connect to a PostgreSQL instance running on `localhost:5432` with a database named `purchaseorder` and credentials `purchaseorder/purchaseorder`. Update the properties in `src/main/resources/application.yml` if your environment differs.

### Running PostgreSQL with Docker Compose

For local development the repository provides a Compose file named `docker-compose.postgres.yml`. The configuration works on macOS, Linux, and Windows (Docker Desktop) because it relies on standard Docker Compose features rather than shell scripts.

Start the database:

```bash
docker compose -f docker-compose.postgres.yml up -d
```

This launches a container named `purchaseorder-postgres` that exposes PostgreSQL on port `5432`, seeds the default credentials, and persists data inside a named Docker volume so that your data survives container restarts.

To stop the database run `docker compose -f docker-compose.postgres.yml down`. Add the `--volumes` flag if you also want to delete the stored data.

#### Configuration

You can override any of the default values by creating a `.env` file in the project root (Compose reads it automatically) and setting the desired variables:

| Variable | Description | Default |
| --- | --- | --- |
| `CONTAINER_NAME` | Name of the Docker container | `purchaseorder-postgres` |
| `POSTGRES_DB` | Database name | `purchaseorder` |
| `POSTGRES_USER` | Database username | `purchaseorder` |
| `POSTGRES_PASSWORD` | Database password | `purchaseorder` |
| `POSTGRES_PORT` | Host port to expose PostgreSQL | `5432` |
| `POSTGRES_IMAGE` | Docker image to use | `postgres:15-alpine` |

Example `.env` file:

```dotenv
POSTGRES_PASSWORD=supersecret
POSTGRES_PORT=55432
```

## Running the Application

With PostgreSQL available (see the Docker Compose instructions above), start the Spring Boot API on port `8080`:

```bash
mvn spring-boot:run
```

The service will be reachable at `http://localhost:8080`.

## API Usage

All business endpoints require a JSON Web Token (JWT). Use the seeded administrator account (`admin@example.com` / `password`) to authenticate and obtain a token before calling any secured route.【F:src/main/resources/db/changelog/db.changelog-1.0.yaml†L93-L118】

### 1. Authenticate and retrieve a token

```bash
curl --request POST \
  --url http://localhost:8080/api/auth/login \
  --header 'Content-Type: application/json' \
  --data '{
    "email": "admin@example.com",
    "password": "password"
  }'
```

The response body contains a `token` field. Include it in subsequent calls:

```text
Authorization: Bearer <token>
```

### 2. Call the business APIs

Replace `<token>` with the value returned from the login endpoint.

#### Users

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/users` | List all users. |
| `GET` | `/api/users/{id}` | Fetch a single user by identifier. |
| `POST` | `/api/users` | Create a new user from a `UserRequest` payload (first name, last name, email, phone, password). |
| `PUT` | `/api/users/{id}` | Update an existing user with a `UserRequest` payload. |
| `DELETE` | `/api/users/{id}` | Remove a user. |

Example request:

```bash
curl --request GET \
  --url http://localhost:8080/api/users \
  --header "Authorization: Bearer <token>"
```

#### Items

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/items` | List all items. |
| `GET` | `/api/items/{id}` | Fetch an item by identifier. |
| `POST` | `/api/items` | Create an item using an `ItemRequest` payload (name, description, price). |
| `PUT` | `/api/items/{id}` | Update an item using an `ItemRequest` payload. |
| `DELETE` | `/api/items/{id}` | Remove an item. |

#### Purchase Orders

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/purchase-orders` | List purchase order headers. |
| `GET` | `/api/purchase-orders/{id}` | Fetch a purchase order by identifier. |
| `POST` | `/api/purchase-orders` | Create a purchase order from a `PurchaseOrderRequest` payload (header information plus line items). |
| `PUT` | `/api/purchase-orders/{id}` | Update a purchase order using a `PurchaseOrderRequest` payload. |
| `DELETE` | `/api/purchase-orders/{id}` | Remove a purchase order. |

> **Tip:** Swagger UI is available at `http://localhost:8080/swagger-ui.html` for interactive documentation once the application is running.
