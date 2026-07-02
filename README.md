# JobTrak

JobTrak is a full-stack AI-powered job application platform that helps users manage job applications, store resumes, track application progress, and analyze resumes against job descriptions using the Gemini API.

The application is built with a React frontend, Java Spring Boot backend, PostgreSQL database, Docker Compose for containerized local development, and GitHub Actions for CI/CD.

---

## Overview

JobTrak helps job seekers organize their job search in one place.

Users can create an account, save resumes, add job applications, update application statuses, and use Gemini API-powered resume analysis to compare a resume against a job description. The platform helps users identify missing skills, improve resume alignment, and better understand how well their resume matches a specific role.

---

## Features

### Authentication

* User signup
* User login
* Secure password handling
* JWT-based authentication
* Protected user-specific data

### Resume Management

* Create and save resumes
* View saved resumes
* Update resume content
* Delete resumes
* Reuse saved resumes for job-specific analysis

### Job Application Tracking

* Add job applications
* Track company name, role, location, notes, and application status
* Update application status as the process moves forward
* Manage applications from a centralized dashboard

Supported application statuses include:

* Saved
* Applied
* Interview
* Rejected
* Offer

### Gemini API Resume Analysis

* Analyze a resume against a specific job description
* Identify matched skills and missing keywords
* Generate targeted resume improvement suggestions
* Provide job-specific feedback to improve resume alignment
* Store analysis results for later review

### Dashboard

* View job application activity
* Access saved resumes
* Navigate between applications, resumes, and AI analysis
* Manage the job search workflow from one interface

### Dockerized Local Development

* PostgreSQL runs in a container
* Spring Boot backend runs in a container
* React frontend runs in a container
* Docker Compose starts the full local stack

### CI/CD

* GitHub Actions workflow for continuous integration
* Backend test job
* Frontend lint and build job
* Docker Compose validation
* Docker image build check

---

## Tech Stack

### Frontend

* React
* Vite
* JavaScript
* CSS
* Axios
* React Router

### Backend

* Java 17
* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* Maven

### Database

* PostgreSQL

### AI Integration

* Gemini API

### DevOps

* Docker
* Docker Compose
* GitHub Actions

---

## Project Structure

```text
JobTrak/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/jobtrak/backend/
│   │   │   │   ├── ai/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   └── BackendApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── components/
│   │   └── App.jsx
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.js
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## Backend Architecture

The backend follows a layered Spring Boot architecture.

### Controller Layer

Handles HTTP requests and returns responses to the frontend.

Examples:

* Authentication endpoints
* Resume endpoints
* Job application endpoints
* AI analysis endpoints

### Service Layer

Contains business logic.

Examples:

* Registering users
* Validating login requests
* Managing resumes
* Managing job applications
* Calling Gemini API for resume analysis

### Repository Layer

Handles database operations using Spring Data JPA.

Examples:

* Finding users by email
* Saving resumes
* Retrieving job applications
* Storing AI analysis results

### Entity Layer

Represents database tables.

Core entities include:

* User
* Resume
* JobApplication
* ApplicationStatus
* AiAnalysis

### DTO Layer

Defines request and response objects used by the API.

DTOs help keep API input/output separate from database entities.

### Security Layer

Handles authentication and authorization logic.

Security responsibilities include:

* JWT generation
* JWT validation
* Protected routes
* User-specific access control

### AI Layer

Handles Gemini API integration and resume analysis logic.

Responsibilities include:

* Sending resume and job description content to Gemini API
* Processing AI responses
* Returning structured resume analysis feedback
* Supporting fallback behavior when needed

---

## Main User Flow

1. User signs up or logs in.
2. User adds a resume.
3. User adds or tracks a job application.
4. User provides a job description.
5. JobTrak sends the resume and job description to Gemini API.
6. Gemini API returns resume feedback and improvement suggestions.
7. User reviews the analysis and updates their resume or application strategy.
8. User tracks application progress through the dashboard.

---

## API Overview

### Authentication

```http
POST /api/auth/signup
POST /api/auth/login
```

### Resumes

```http
POST /api/resumes
GET /api/resumes
GET /api/resumes/{id}
PUT /api/resumes/{id}
DELETE /api/resumes/{id}
```

### Job Applications

```http
POST /api/applications
GET /api/applications
GET /api/applications/{id}
PUT /api/applications/{id}
DELETE /api/applications/{id}
```

### AI Analysis

```http
POST /api/ai/analyze
GET /api/ai/analysis
GET /api/ai/analysis/{id}
```

Endpoint names may vary slightly depending on the final controller mappings.

---

## Local Development Setup

### Prerequisites

Install the following:

* Java 17
* Node.js
* Docker
* Docker Compose
* Git

PostgreSQL can be installed locally, but Docker Compose can also start PostgreSQL for the project.

---

## Running the Full App with Docker

From the project root:

```bash
docker compose up --build
```

This starts:

* PostgreSQL database
* Spring Boot backend
* React frontend

Default local ports:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Database: localhost:5433
```

To stop the containers:

```bash
docker compose down
```

To stop containers and remove database volume:

```bash
docker compose down -v
```

---

## Running Backend Locally Without Docker

Navigate to the backend folder:

```bash
cd backend
```

Run the backend:

```bash
./mvnw.cmd spring-boot:run
```

For PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Running Frontend Locally Without Docker

Navigate to the frontend folder:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

## Environment Variables

The backend uses environment variables for sensitive configuration.

Create a backend `.env` file using `.env.example` as a guide.

Example backend environment variables:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jobtrak_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_database_password
JWT_SECRET=your_jwt_secret
GEMINI_API_KEY=your_gemini_api_key
```

Never commit real secrets, passwords, JWT secrets, or API keys to GitHub.

---

## Docker Compose Services

The Docker Compose setup includes:

### postgres

Runs PostgreSQL for local development.

### backend

Builds and runs the Spring Boot backend.

### frontend

Builds and runs the React frontend through Nginx.

---

## CI/CD

The project includes a GitHub Actions CI workflow.

The CI pipeline runs on pushes and pull requests.

CI jobs include:

### Backend Tests

* Sets up Java 17
* Starts PostgreSQL service
* Runs Maven backend tests

### Frontend Lint and Build

* Sets up Node.js
* Installs frontend dependencies
* Runs frontend lint checks
* Builds the React frontend

### Docker Build

* Validates Docker Compose configuration
* Builds Docker images for the application

---

## Database

JobTrak uses PostgreSQL as the relational database.

Main data models include:

### User

Stores registered user account information.

### Resume

Stores user-created resume content.

### JobApplication

Stores tracked job applications.

### ApplicationStatus

Defines the status of a job application.

### AiAnalysis

Stores Gemini API-generated resume analysis results.

---

## Gemini API Usage

JobTrak uses Gemini API to analyze resumes against job descriptions.

The Gemini-powered analysis helps users:

* Understand how well their resume matches a role
* Identify missing keywords
* Improve resume bullet points
* Strengthen job-specific positioning
* Generate more targeted application material

The system is designed to assist the user, not replace user judgment. Users should review AI-generated suggestions before applying.

---

## Security

Security features include:

* Password hashing
* JWT authentication
* Protected backend routes
* User-specific data access
* Input validation
* Environment-based secret management

---

## Testing

Backend testing is handled through Maven.

Run backend tests:

```bash
cd backend
./mvnw.cmd test
```

Frontend checks:

```bash
cd frontend
npm run lint
npm run build
```

---

## Development Phases Completed

### Phase 1 — Foundation

* React frontend setup
* Spring Boot backend setup
* PostgreSQL setup
* GitHub repository setup

### Phase 2 — Backend Core

* Layered Spring Boot architecture
* Entities
* Repositories
* Services
* Controllers
* DTO structure

### Phase 3 — Resume and Job Tracking

* Resume management
* Job application tracking
* Application status support

### Phase 4 — Gemini API Analysis

* Gemini API integration
* Resume and job description analysis
* AI analysis result handling

### Phase 5 — Frontend Integration

* Login page
* Signup page
* Dashboard page
* Resume page
* Applications page
* AI analysis page

### Phase 6 — Docker

* Backend Dockerfile
* Frontend Dockerfile
* Docker Compose setup
* PostgreSQL container
* Full local containerized stack

### Phase 7 — CI/CD

* GitHub Actions CI workflow
* Backend tests
* Frontend lint and build
* Docker build validation

---

## Planned Improvements

### Cloud Deployment

Planned deployment target:

* Frontend: Vercel
* Backend: Render
* Database: Neon PostgreSQL

### Future Features

* Resume PDF upload
* DOCX resume parsing
* Cover letter generation
* Advanced dashboard analytics
* Resume version comparison
* Interview preparation questions
* Email reminders for follow-ups
* Better Gemini prompt templates
* Production deployment
* End-to-end testing

---

## Project Purpose

JobTrak was built to demonstrate practical full-stack engineering skills using modern backend, frontend, database, AI, Docker, and CI/CD workflows.

The project demonstrates:

* Java backend development with Spring Boot
* REST API design
* PostgreSQL database modeling
* Authentication and authorization
* React frontend development
* Gemini API integration
* Docker-based development
* CI/CD with GitHub Actions
* Production-style project structure

---

## Author

**Shiven Bhardwaj**
GitHub: sbhardw3
