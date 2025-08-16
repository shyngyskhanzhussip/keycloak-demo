# Keycloak Demo Project

This is a full-stack demo project showcasing Keycloak integration with a Spring Boot backend and Angular frontend.

## Project Structure

```
keycloak-demo/
├── backend/          # Spring Boot application
│   ├── src/
│   ├── pom.xml
│   └── mvnw
└── frontend/         # Angular application
    ├── src/
    ├── package.json
    └── angular.json
```

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6 or higher
- Angular CLI 16 or higher

## Getting Started

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The backend will be available at `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run the development server:
   ```bash
   ng serve
   ```

The frontend will be available at `http://localhost:4200`

## Keycloak Configuration

This project is designed to work with Keycloak for authentication and authorization. You'll need to:

1. Set up a Keycloak server
2. Create a realm and client
3. Configure the application properties

## Development

- Backend: Spring Boot with Spring Security and Keycloak integration
- Frontend: Angular with Angular Material and Keycloak JS adapter

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.
