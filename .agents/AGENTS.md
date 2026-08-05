# AGENTS.md

This file provides guidelines and rules for AI coding assistants (Antigravity, Gemini Agent, etc.) when working in this repository.

## Tech Stack

- **Frontend**: React (Vite, JSX/JS or TSX/TS, npm)
- **Backend**: Java 17+ (Spring Boot, Maven)
- **Package Managers**: npm for frontend and Maven (`pom.xml`) for backend.

---

## Repository & Development Rules

### 1. Git Rules
- Never create git branches or commits automatically unless explicitly requested by the user.
- Git command usage must be strictly read-only (`git status`, `git diff`, `git log`, `git show`) unless instructed otherwise.

### 2. Documentation & Commenting Standards
- **Method & Code Explanations**: A descriptive comment must be added prior to each created method explaining its general purpose and functionality. Inside the method, comment only key or complex lines that require clarification, avoiding trivial line-by-line comments.
- **No Emojis**: The use of any type of emoji in code, comments, system messages, or documentation is strictly prohibited.
- **Comment Language**: All code comments and explanations must be written exclusively in Spanish.

### 3. Backend Best Practices (Java & Spring Boot)
- **Clean Architecture & SOLID Principles**: Maintain strict separation of concerns among controllers, services, repositories, and data transfer objects (DTOs).
- **Naming Conventions**:
  - Classes and interfaces in `PascalCase` (e.g. `UsuarioController.java`, `PagoService.java`).
  - Methods and variables in `camelCase` (e.g. `obtenerDetalleUsuario`, `montoTotal`).
  - Constants in `UPPER_SNAKE_CASE`.
- **Error Handling**: Implement global exception handlers (`@RestControllerAdvice`) and return standardized HTTP responses.
- **No Dead Code**: Remove commented-out code and unused imports.

### 4. Frontend Best Practices (React & Vite)
- **Functional Components & Modern Hooks**: Use functional components powered by React Hooks (`useState`, `useEffect`, `useCallback`, `useMemo`).
- **Code Cleanliness**: Avoid `console.log` statements in active development or production code.
- **Async Request Management**: Manage loading, success, and error states for all HTTP REST API interactions.
- **File Naming Conventions**: Name component and style files consistently (`kebab-case` or `PascalCase` following project standards).

---

## Main Commands

### Backend (Spring Boot / Maven)
```bash
# Run backend development server (port 8080)
mvn spring-boot:run

# Compile project and run unit tests
mvn clean test

# Build final executable package
mvn clean package
```

### Frontend (React / Vite)
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start frontend development server (port 5173)
npm run dev

# Build static assets for production
npm run build
```
