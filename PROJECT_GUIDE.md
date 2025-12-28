# 🎯 HackerHub - Complete Project Guide

Welcome to **HackerHub** - A full-stack web scraper for discovering hackathons and certifications!

---

## 📁 Project Overview

This project consists of two main components:

### 1. **Backend** - `hackhub_scraper_java/`
- **Technology**: Java 23 + Spring Boot 3.5.5
- **Database**: MySQL 8
- **Purpose**: Web scraping service for MLH and Devpost hackathons
- **Port**: 8080
- **Status**: ✅ Already working on your machine

### 2. **Frontend** - `hackerhub_FE/`
- **Technology**: Next.js 15.5.9 + React 19 + TypeScript 5
- **Purpose**: User interface for searching and displaying hackathons
- **Port**: 9002
- **Status**: ✅ Newly configured and ready to run

---

## 🚀 Quick Start (Both Servers)

### Step 1: Start MySQL Database
Ensure MySQL is running on `localhost:3306` with database `hackhub`.

### Step 2: Start Backend (Terminal 1)
```powershell
cd c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java
mvn spring-boot:run
```
**Expected output:**
```
Started HackHubApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

### Step 3: Start Frontend (Terminal 2)
```powershell
cd c:\Users\hp\Desktop\hackerhub\hackerhub_FE
npm run dev
```
**Expected output:**
```
▲ Next.js 15.5.9
- Local: http://localhost:9002
✓ Ready in X.XXs
```

### Step 4: Open Browser
Navigate to: **http://localhost:9002**

---

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         USER BROWSER                          │
│                    http://localhost:9002                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ HTTP Requests
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   FRONTEND (Next.js + React)                 │
│                         Port: 9002                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  - Search Form UI                                     │   │
│  │  - Event Display Grid                                 │   │
│  │  - Filters & Search Logic                             │   │
│  │  - Server Actions (API calls)                         │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ POST /api/scraper/mlh
                         │ POST /api/scraper/devpost
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              BACKEND (Spring Boot + Java)                    │
│                      Port: 8080                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controllers:                                         │   │
│  │   - ScraperController (/api/scraper/*)                │   │
│  │                                                        │   │
│  │  Services:                                            │   │
│  │   - ScraperService (orchestrates scraping)            │   │
│  │   - MLHScraperService (scrapes MLH.io)                │   │
│  │   - DevpostService (scrapes Devpost API)              │   │
│  │                                                        │   │
│  │  Repositories:                                        │   │
│  │   - EventRepository (JPA data access)                 │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ JDBC
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   MySQL Database                             │
│                    localhost:3306                            │
│                    Database: hackhub                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Tables:                                              │   │
│  │   - events (hackathons & certifications)              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ Web Scraping
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              External APIs & Websites                        │
│   - MLH.io (https://mlh.io/seasons/2026/events)              │
│   - Devpost API (https://devpost.com/api/hackathons)         │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Data Flow Example

1. **User** opens http://localhost:9002
2. **User** fills search form (location: "Online", count: 10)
3. **Frontend** calls server action `scrapeHackathons()`
4. **Server action** makes 2 parallel POST requests:
   - `POST http://localhost:8080/api/scraper/mlh`
   - `POST http://localhost:8080/api/scraper/devpost`
5. **Backend** receives requests at `ScraperController`
6. **ScraperService** delegates to specific scrapers:
   - `MLHScraperService` scrapes MLH.io
   - `DevpostService` scrapes Devpost API
7. **Scrapers** extract hackathon data
8. **Backend** saves to MySQL database (optional)
9. **Backend** returns event data as JSON
10. **Frontend** receives combined results
11. **Frontend** displays events in grid layout
12. **User** sees hackathons!

---

## 🛠️ Technology Stack

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 15.5.9 | React framework with SSR |
| React | 19.2.1 | UI library |
| TypeScript | 5.x | Type safety |
| Tailwind CSS | 3.4.1 | Styling |
| Radix UI | Latest | Accessible components |
| Zod | 3.24.2 | Schema validation |

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 23 | Programming language |
| Spring Boot | 3.5.5 | Web framework |
| Spring Data JPA | 3.5.5 | Database ORM |
| MySQL | 8.x | Database |
| Selenium | 4.16.1 | Web scraping (dynamic) |
| JSoup | 1.17.2 | Web scraping (static) |
| Gson | 2.11.0 | JSON parsing |

---

## 📝 Available Endpoints

### Backend API Endpoints

#### 1. Scrape MLH Hackathons
```http
POST http://localhost:8080/api/scraper/mlh
Content-Type: application/json

{
  "title": "",           // Optional filter
  "prize": "",           // Optional filter
  "location": "Online",  // Optional filter
  "count": 10            // Number of results (5-50)
}
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "MLH Hackathon 2025",
    "description": "Full description...",
    "url": "https://mlh.io/...",
    "location": "Online",
    "date": "2025-02-15",
    "provider": "MLH",
    "type": "HACKATHON",
    "scrappedAt": "2025-12-27T02:00:00"
  }
]
```

#### 2. Scrape Devpost Hackathons
```http
POST http://localhost:8080/api/scraper/devpost
Content-Type: application/json

{
  "title": "",
  "location": "",
  "count": 10
}
```

---

## 📚 Documentation Index

### Frontend Documentation
- **`hackerhub_FE/QUICKSTART.md`** - Fast track guide ⚡
- **`hackerhub_FE/SETUP.md`** - Detailed setup instructions 📖
- **`hackerhub_FE/BACKEND_INTEGRATION.md`** - API connection guide 🔌
- **`hackerhub_FE/CONFIGURATION_SUMMARY.md`** - All changes made ✅

### Backend Documentation
- **`hackhub_scraper_java/README.md`** - Backend overview & usage
- **`hackhub_scraper_java/QUICKSTART.md`** - Backend quick start
- **`hackhub_scraper_java/POSTMAN.md`** - API testing guide

---

## 🎨 Frontend Features

✅ **Hero Section** - Eye-catching landing page  
✅ **Search Form** - Filter by title, prize, location, count  
✅ **Dual Scraping** - MLH + Devpost simultaneously  
✅ **Loading States** - Skeleton loaders for better UX  
✅ **Event Grid** - Beautiful card layout  
✅ **Toast Notifications** - Success/error feedback  
✅ **Responsive Design** - Mobile, tablet, desktop  
✅ **Error Handling** - User-friendly error messages  
✅ **Type Safety** - Full TypeScript support  
✅ **Dark Mode Ready** - Modern UI with Tailwind  

---

## 🔧 Backend Features

✅ **RESTful API** - Clean endpoint design  
✅ **Dual Provider Support** - MLH + Devpost scrapers  
✅ **Selenium Integration** - Handle dynamic content  
✅ **Database Persistence** - MySQL with JPA  
✅ **Retry Logic** - Exponential backoff for failed requests  
✅ **Rate Limiting** - Respectful scraping  
✅ **Deep Scraping** - Extract comprehensive details  
✅ **Oracle Certificates** - Support for certification scraping  
✅ **Error Handling** - Robust exception management  

---

## 🔍 Configuration Changes Made

### What Was Configured in Frontend:

1. ✅ **Fixed missing `toast` import** in `src/app/page.tsx`
   - Added: `const { toast } = useToast();`

2. ✅ **Updated API integration** in `src/app/actions.ts`
   - Added environment variable support
   - Default backend URL: `http://localhost:8080`
   - Better error messages with backend URL

3. ✅ **Removed Turbopack** from `package.json`
   - Fixed Windows compatibility issue
   - Changed: `"dev": "next dev -p 9002"`

4. ✅ **Installed dependencies**
   - 957 packages installed
   - All 10 vulnerabilities fixed

5. ✅ **Created documentation**
   - QUICKSTART.md
   - SETUP.md
   - BACKEND_INTEGRATION.md
   - CONFIGURATION_SUMMARY.md

---

## 🧪 Testing Checklist

### Frontend Tests:
- [ ] Homepage loads at http://localhost:9002
- [ ] Search form is visible and interactive
- [ ] Browser console shows no errors
- [ ] TypeScript compiles without errors

### Backend Tests:
- [ ] Spring Boot starts on port 8080
- [ ] MySQL database connection works
- [ ] Endpoints respond to POST requests
- [ ] Data is saved to database

### Integration Tests:
- [ ] Frontend can call backend API
- [ ] API responses display in UI
- [ ] Loading states work correctly
- [ ] Error messages display properly
- [ ] Both MLH and Devpost scraping works

---

## 🐛 Common Issues

### Issue 1: "Cannot connect to backend"
**Solution**: Start the Spring Boot server
```powershell
cd c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java
mvn spring-boot:run
```

### Issue 2: "MySQL connection error"
**Solution**: Start MySQL and create database
```sql
CREATE DATABASE IF NOT EXISTS hackhub;
```

### Issue 3: "Port already in use"
**Solution**: Stop the process or change port in `package.json`

### Issue 4: "npm install fails"
**Solution**: Clear cache and retry
```powershell
npm cache clean --force
npm install
```

---

## 📈 Future Enhancements

### Frontend
- [ ] Add user authentication
- [ ] Implement favorites functionality
- [ ] Add filters for date range
- [ ] Export results to PDF/CSV
- [ ] Social sharing features

### Backend
- [ ] Add more scraping providers
- [ ] Implement caching layer
- [ ] Add GraphQL support
- [ ] Scheduled scraping jobs
- [ ] Webhook notifications

---

## 🆘 Support & Resources

- **Frontend Issues**: Check `hackerhub_FE/SETUP.md`
- **Backend Issues**: Check `hackhub_scraper_java/README.md`
- **API Testing**: Use Postman (see `POSTMAN.md`)
- **Next.js Docs**: https://nextjs.org/docs
- **Spring Boot Docs**: https://spring.io/projects/spring-boot

---

## 🎉 You're All Set!

Both frontend and backend are configured and ready to run!

### Current Status:
- ✅ Frontend: Configured and dependencies installed
- ✅ Backend: Already working (no changes needed)
- ✅ Documentation: Complete guides created

### Next Steps:
1. Start MySQL database
2. Start backend server (`mvn spring-boot:run`)
3. Start frontend server (`npm run dev`)
4. Open http://localhost:9002
5. Start searching for hackathons! 🎯

---

**Project**: HackerHub  
**Configured**: December 27, 2025  
**Status**: ✅ Ready for Development  
**Frontend Port**: 9002  
**Backend Port**: 8080  

Happy Coding! 🚀
