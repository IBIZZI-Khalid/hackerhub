# 🎯 HackerHub - Hackathon & Certification Discovery Platform

A full-stack web application for discovering hackathons and tech certifications from multiple sources.

## 👥 Authors

| Name | Role | GitHub |
|------|------|--------|
| IBIZZI-Khalid | Developer | [@IBIZZI-Khalid](https://github.com/IBIZZI-Khalid) |
| Hanae OUKACHA | Developer | [@HanaeOukacha1](https://github.com/HanaeOukacha1) |

**Repository**: [github.com/IBIZZI-Khalid/hackerhub](https://github.com/IBIZZI-Khalid/hackerhub)

---

## 📁 Project Structure

```
hackerhub/
├── hackhub_scraper_java/   # Spring Boot Backend (Java 23)
├── hackerhub_FE/           # Next.js Frontend (React 19)
├── docker-compose.yml      # Docker orchestration
└── mysql-init/             # Database initialization
```

---

## 🚀 Quick Start with Docker

**Prerequisites**: Docker Desktop installed and running

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/hackerhub.git
cd hackerhub

# Start all services
docker compose up -d

# Wait for services to be ready (~2-3 mins on first run)
docker compose logs -f
```

**Access the application:**
- 🌐 Frontend: http://localhost:9002
- ⚙️ Backend API: http://localhost:8080
- 🗃️ MySQL: localhost:3307

**Stop all services:**
```bash
docker compose down
```

---

## 🛠️ Technology Stack

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 15.x | React framework with SSR |
| React | 19.x | UI library |
| TypeScript | 5.x | Type safety |
| Tailwind CSS | 3.x | Styling |

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 23 | Programming language |
| Spring Boot | 3.x | Web framework |
| Selenium | 4.x | Web scraping |
| MySQL | 8.x | Database |

---

## 📊 Features

### Frontend
- ✅ Real-time event streaming during scraping
- ✅ User authentication (JWT)
- ✅ Personalized recommendations
- ✅ Event bookmarking/favorites
- ✅ Responsive dark mode UI

### Backend
- ✅ Multi-source scraping (Devpost, MLH, Oracle, IBM, Microsoft)
- ✅ Server-Sent Events (SSE) for live updates
- ✅ Content-based recommendation engine
- ✅ Secure REST API with CORS

---

## 📝 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | User authentication |
| GET | `/api/scraper/stream/devpost` | Stream Devpost hackathons |
| GET | `/api/scraper/stream/mlh` | Stream MLH hackathons |
| GET | `/api/recommendations` | Get personalized recommendations |
| GET | `/api/interactions/bookmarks` | Get user bookmarks |

---

## 🔄 Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend   │────▶│   Backend    │────▶│    MySQL     │
│  (Next.js)   │     │ (Spring Boot)│     │   Database   │
│   :9002      │     │    :8080     │     │    :3306     │
└──────────────┘     └──────────────┘     └──────────────┘
                            │
                            ▼
                   ┌──────────────────┐
                   │  External Sites  │
                   │ Devpost, MLH,    │
                   │ Oracle, IBM, MS  │
                   └──────────────────┘
```

---

## 📄 License

This project is for educational purposes.

---

**Configured**: January 2026  
**Status**: ✅ Production Ready
