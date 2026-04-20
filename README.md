# Payment Service

Spring Boot сервис для управления платежами с REST API, обработкой сообщений из RabbitMQ, идемпотентностью запросов и базовым rate limit.

## Стек

- Java 21
- Spring Boot 3
- Spring Web, Spring Data JPA, Spring AMQP
- H2 (in-memory)
- Resilience4j (Circuit Breaker)
- Bucket4j (rate limiting)
- Springdoc OpenAPI
- Checkstyle

## Запуск проекта

### Вариант 1: через Docker Compose (рекомендуется)

Запускает сразу `payment-service` и `rabbitmq`.

```bash
docker compose up --build
```

Остановка:

```bash
docker compose down
```

RabbitMQ Management UI: `http://localhost:15672`  
Логин/пароль: `admin/admin`

### Вариант 2: локально через Gradle

Требования:
- Java 21
- Gradle (или Gradle Wrapper)
- Доступный RabbitMQ на `localhost:5672` с `admin/admin`

Запуск RabbitMQ в Docker (если локально не установлен):

```bash
docker run -d --name payment-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin \
  rabbitmq:3.13-management
```

Запуск приложения:

```bash
./gradlew bootRun
```

### Проверка, что сервис поднялся

Приложение доступно на `http://localhost:8082`.

Полезные URL:
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- H2 console: `http://localhost:8082/h2-console`

## API платежей

Базовый путь: `/api/payments`

### Создать платеж

```bash
curl -i -X POST "http://localhost:8082/api/payments" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-create-1001" \
  -d '{
    "orderId": 1001,
    "status": "PENDING",
    "method": "CARD",
    "amount": {
      "amount": 1499.99,
      "currency": "RUB"
    }
  }'
```

### Получить все платежи

```bash
curl -i "http://localhost:8082/api/payments"
```

### Получить платеж по ID

```bash
curl -i "http://localhost:8082/api/payments/1"
```

### Обновить платеж

```bash
curl -i -X PUT "http://localhost:8082/api/payments/1" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "status": "CAPTURED",
    "method": "CARD",
    "amount": {
      "amount": 1499.99,
      "currency": "RUB"
    }
  }'
```

### Удалить платеж

```bash
curl -i -X DELETE "http://localhost:8082/api/payments/1"
```

## Конфигурация

Основные настройки находятся в `src/main/resources/application.properties`:
- HTTP порт: `server.port=8082`
- H2 datasource
- RabbitMQ `spring.rabbitmq.*`
- `rabbitmq.payment.*` для exchange/queue
- Circuit Breaker `resilience4j.circuitbreaker.*`

Логирование настраивается в `src/main/resources/logback-spring.xml`.

## Проверка качества кода

Запуск Checkstyle:

```bash
./gradlew checkstyleMain checkstyleTest
```

Общий quality-check:

```bash
./gradlew check
```
