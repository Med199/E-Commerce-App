#  E-Commerce Backend API

A Spring Boot backend application for an e-commerce system implementing JWT authentication using HTTP cookies, user management, and product-related modules.

This project demonstrates real-world backend development concepts such as authentication, authorization, REST APIs, and relational database modeling.

---

## 📌 About the Project

This backend API provides core functionality for an e-commerce system including user authentication, role-based access control, product management, and secure session handling using JWT stored in cookies.

---

## ✨ Features
- User registration (sign up)
- User authentication (sign in / sign out)
- JWT-based authentication using HTTP cookies
- Secure password hashing with BCrypt
- Spring Security integration
- Clean layered architecture (Controller → Service → Repository)
- DTO-based API design for request/response models
- Global exception handling for consistent API error responses

### 👤 User Module
- User entity management
- Role-based access control (User / Seller / Admin)
- Fetch authenticated user details (current user endpoint)

### 🛍️ Product Module
- Create product
- Update product
- Delete product
- Get all products
- Get product by Category/keywords

### 🗂️ Category Module (if implemented)
- Create category
- Update category
- Delete category
- Get all categories

---

## 🧰 Tech Stack

- Java 17
- Spring Boot
- Spring Security
- JWT (stored in cookies)
- Spring Data JPA (Hibernate)
- MySQL
- Maven
  
---

## 📁 Project Structure

src/
├── config/
├── controller/
├── exception/
├── model/
├── payload/
├── repositories/
├── security/
│   ├── configuration/
│   ├── jwt/
│   ├── request/
│   ├── response/
│   ├── services/
├── service/

---

## 🚧 Future Improvements

- Shopping cart functionality
- Order management system
- Payment integration
- Swagger  documentation
- Unit and integration testing.
- Frontend ( React )
- Deployment (Docker / Cloud)

---

## 👨‍💻 Author

GitHub: https://github.com/Med199
