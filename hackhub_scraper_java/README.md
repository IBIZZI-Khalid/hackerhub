# HackHub Backend - Event Scraper API

Spring Boot backend service for scraping and serving hackathon events from multiple sources.

## Features

- **Multi-source Scraping**: Devpost, MLH, Oracle, IBM, Microsoft
- **Real-time Streaming**: SSE (Server-Sent Events) for live scraping updates
- **Recommendation Engine**: Content-based and collaborative filtering
- **JWT Authentication**: Secure user authentication
- **MySQL Database**: Persistent event storage

## Tech Stack

- Java 23
- Spring Boot 3.x
- Selenium WebDriver
- MySQL 8.0
- Docker

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Scraping
- `GET /api/scraper/stream/devpost` - Stream Devpost hackathons
- `GET /api/scraper/stream/mlh` - Stream MLH hackathons
- `GET /api/scraper/stream/oracle` - Stream Oracle certifications
- `GET /api/scraper/stream/ibm` - Stream IBM events
- `GET /api/scraper/stream/microsoft` - Stream Microsoft events

### Recommendations
- `GET /api/recommendations` - Get personalized recommendations
- `GET /api/recommendations/similar/{id}` - Get similar events

### Interactions
- `GET /api/interactions/bookmarks` - Get user bookmarks
- `POST /api/interactions` - Record user interaction

## Running

```bash
# With Maven
mvn spring-boot:run

# With Docker
docker compose up backend
```

## Configuration

Environment variables:
- `SPRING_DATASOURCE_URL` - MySQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
