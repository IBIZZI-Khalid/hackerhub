# HackHub Explorer

A comprehensive platform for discovering and tracking hackathons, certifications, and tech events from various sources.

## Features

- **Real-time Event Scraping**: Automated scraping from Devpost, MLH, Oracle, IBM, and Microsoft
- **Smart Recommendations**: Personalized event recommendations based on user preferences
- **User Authentication**: Secure JWT-based authentication system
- **Event Tracking**: Save and track your favorite events
- **Responsive Design**: Modern UI built with Next.js and Tailwind CSS

## Tech Stack

### Frontend
- Next.js 15 (React 19)
- TypeScript
- Tailwind CSS
- Shadcn/ui Components

### Backend
- Spring Boot 3.x
- Java 23
- MySQL Database
- Selenium WebDriver (for scraping)

## Getting Started

### Prerequisites
- Node.js 20+
- Java 23+
- Docker & Docker Compose
- MySQL 8.0+

### Running with Docker

```bash
# Start all services
docker compose up -d

# Access the application
# Frontend: http://localhost:9002
# Backend API: http://localhost:8080
```

### Manual Setup

**Frontend:**
```bash
cd hackerhub_FE
npm install
npm run dev
```

**Backend:**
```bash
cd hackhub_scraper_java
mvn spring-boot:run
```

## Project Structure

```
hackerhub/
├── hackerhub_FE/          # Next.js Frontend
├── hackhub_scraper_java/  # Spring Boot Backend
├── docker-compose.yml     # Docker orchestration
└── mysql-init/            # Database initialization
```

## API Endpoints

- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration  
- `GET /api/scraper/stream/{provider}` - Stream scraped events
- `GET /api/recommendations` - Get personalized recommendations
- `GET /api/interactions/bookmarks` - Get saved events

## License

This project is for educational purposes.
