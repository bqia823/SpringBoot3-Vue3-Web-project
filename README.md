# SpringBoot3-Vue3-Web-project

## My Project Backend

A full-stack template project built with **SpringBoot 3** and **Vue 3**, featuring a decoupled front-end and back-end architecture. The project integrates various technology stacks and uses **JWT for authentication**.


### Backend Features and Technical Points

- **User Registration, Login, and Password Reset**: Provides essential functionalities and corresponding APIs.
- **MyBatis-Plus**: Used as the persistence layer framework for easier data manipulation.
- **Redis**: Stores verification codes for registration and password reset operations with expiration time control.
- **RabbitMQ**: Queues SMS sending tasks, which are then handled by a unified listener.
- **Spring Security**: Used as the authorization framework, manually integrated with JWT for authentication.
- **IP Rate Limiting**: Implements IP rate limiting with Redis to prevent abuse.
- **Separation of View and Data Layer Objects**: Utility methods utilize reflection for quick conversion between view and data layer objects.
- **Unified JSON Error Responses**: Error and exception pages return JSON format for consistent front-end handling.
- **Manual Cross-Origin Handling**: Implemented using a filter.
- **Swagger**: Auto-generates API documentation, pre-configured for login-related endpoints.
- **Snowflake ID Generation**: Automatically generates a Snowflake ID for each request to facilitate issue tracking.
- **Environment-Specific Configurations**: Separate configurations for development and production environments.
- **Detailed Logging**: Includes complete information for each request along with the corresponding Snowflake ID, supports file logging.
- **Clear Project Structure**: Clear responsibilities, comprehensive comments, and ready-to-use.

### Frontend Features and Technical Points

- **User Interfaces**: Includes user registration, login, password reset screens, and a simple home page.
- **Vue Router**: Used for routing.
- **Axios**: Used for asynchronous requests.
- **Element-Plus**: UI component library.
- **VueUse**: Adapts for dark mode switching.
- **Unplugin Auto Import**: Reduces the bundle size by importing only what's needed.


## Installation and Running

### Prerequisites

- Java 17 and above
- Maven
- Node.js
- Redis
- RabbitMQ

### Backend Setup

1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/my-project-backend.git
    cd my-project-backend
    ```

2. Configure your database and services:
    Update the `application.yml` file with your MySQL, Redis, and RabbitMQ configurations.

3. Build the project:
    ```bash
    mvn clean install
    ```

4. Run the project:
    Run the `MyProjectBackendApplication` file to start the backend.

### Frontend Setup

1. Navigate to the frontend directory:
    ```bash
    cd my-project-frontend
    ```

2. Install dependencies:
    ```bash
    npm install @element-plus/icons-vue @vueuse/core axios element-plus vue vue-router
    npm install --save-dev @vitejs/plugin-vue unplugin-auto-import unplugin-vue-components vite
    ```

3. Run the project:
    ```bash
    npm run dev
    ```

## Project Structure

### Backend

- `src/main/java/com/example`: Main application source code.
- `src/main/resources`: Configuration files and resources.

### Frontend

- `src`: Main application source code for the front-end.
- `public`: Public assets and the main HTML file.

## Technologies Used

### Backend

- Spring Boot 3
- MyBatis-Plus
- Redis
- RabbitMQ
- Spring Security
- JWT
- Swagger
- Lombok

### Frontend

- Vue 3
- Vue Router
- Axios
- Element-Plus
- VueUse
- Unplugin Auto Import

