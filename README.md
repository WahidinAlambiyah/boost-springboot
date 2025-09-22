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
