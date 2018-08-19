## Docker components (infrastructure)

### Postgres SQL
``docker run --name postgres -d -p 5432:5432 -e POSTGRES_PASSWORD=bitcoin -e POSTGRES_USER=cashback postgres:9.6.6-alpine``