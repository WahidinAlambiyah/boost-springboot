# Project Requirements

Create project default landing look & behavior as shown in the provided screen capture using Spring Boot with Java (Maven). The following details outline the required scope.

## 1. Requirements
- REST endpoints:
  - `GET /user/auth/login`
  - `POST /user/auth/register`
  - `POST /menu`
  - `PUT /menu/:id`
  - `DELETE /menu/:id`
  - `POST /order`
  - `PUT /order/:id`
  - `DELETE /order/:id`
  - `GET /order`
- Login untuk customer, create account dan masuk ke akun.
- Login untuk admin, manage detail accounts dan set categories.

## 2. Architecture
- On MVC line and modular, package by feature not by layer.
- DDD, service oriented, clean architecture.
- System ini ada 2 user role, customer dan admin.
- Admin bisa create, read, update, delete produk atau menu.
- Customer dapat membuat account, account terhubung ke user.
- Setiap service dalam fitur memiliki post, delete, update, dan get (standard CRUD).
- Dalam implementasi:
  - DTO / request & response.
  - Entity & repository.
  - Service & service implementation.
  - Controllers.
- Database: PostgreSQL & JOOQ / Hibernate.
- JWT berdasarkan penyimpanan cookie.
- Create response example.
- Gunakan ULID untuk ID management.
- Gunakan event sourcing untuk data management.
- Gunakan graph database: (Neo4J).
- Observability and application log.

## 3. Database Diagram
- User dapat create account, account terhubung dengan user.
- System mempunyai 2 role customer dan admin.
- Admin bisa create read update delete produk / menu.
- Satu user banyak order.
- Satu order mengandung banyak order items.
- Satu order item terkait ke product dan order.
- Account banyak contact person.

## 4. Data Model
- Table users: username unique, email unique, password, role, created_at, updated_at (timestamps).
- Accounts: name, phone, code, address.
- Order.
- OrderItems.
- Order struct:
  - Use ULID.
  - Order items: products, qty, price.
  - Order: total, status.
- Profile: user_id, account_id, name, image_url, passport, date_of_birth, gender, address etc.
- Add transaction table for manage transaction.
- Add categories for choose categories.
- Add account linked to user and contact person users per account.

## 5. UI
- Use `@ConfigurationProperties` for YAML setting and configuration environment.
- Order page:
  - Login user register.
  - Order flow: register -> login -> order.
  - POP UP window user confirmation detail order.
  - Order list / history with status ongoing, done, cancel in UI.
- GUI.

## 6. Misc
- Add PostgreSQL database & Redis as distributed cache.
- Service untuk Redis untuk manage create data order.
- Add endpoint order untuk create order.
- Upload table & order & table order.

## 7. UI (Additional)
- UI login register & pop up login.
- Validate email & password login register validation.
- Add middleware for login & set config property for email & password.
- Device & browser validation for login.
- Add plugin login register Google & GitHub.
- Order card UI create to order show ticket order show pop up detail & contact & req*.
- Add message when user order success, example success messages.
- Add popup register and new order select ticket order capture.

---
Generate project, upload to GitHub & send links to repository.
