# 🐳 HackerHub Docker Setup Guide

This guide explains how to run the entire HackerHub stack (MySQL + Backend + Frontend) using Docker.

---

## 📋 Prerequisites

- **Docker Desktop** installed and running
  - Windows: [Download Docker Desktop](https://www.docker.com/products/docker-desktop/)
  - Verify: `docker --version` and `docker-compose --version`

---

## 🚀 Quick Start

### Option 1: Production Mode (Optimized Build)

```powershell
# Navigate to project root
cd c:\Users\hp\Desktop\hackerhub

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

**Services Started:**
- MySQL Database on port **3306**
- Spring Boot Backend on port **8080**
- Next.js Frontend on port **9002**

**Access the app:** http://localhost:9002

---

### Option 2: Development Mode (Hot Reload)

For development with live code changes:

```powershell
# Start in dev mode
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f
```

**Features:**
- ✅ Frontend hot reload (changes reflect immediately)
- ✅ Backend with debug port 5005
- ✅ Source code mounted as volumes

---

## 🎮 Managing Services

### Start All Services

```powershell
docker-compose up -d
```

The `-d` flag runs in detached mode (background).

---

### Stop All Services

```powershell
docker-compose down
```

To also remove volumes (database data):
```powershell
docker-compose down -v
```

---

### Restart Individual Services

**Restart Frontend Only:**
```powershell
docker-compose restart frontend
```

**Restart Backend Only:**
```powershell
docker-compose restart backend
```

**Restart MySQL Only:**
```powershell
docker-compose restart mysql
```

---

### Rebuild After Code Changes

**Rebuild and restart a specific service:**
```powershell
# Rebuild backend
docker-compose up -d --build backend

# Rebuild frontend
docker-compose up -d --build frontend

# Rebuild everything
docker-compose up -d --build
```

---

## 📊 Monitoring Services

### View Logs

**All services:**
```powershell
docker-compose logs -f
```

**Specific service:**
```powershell
docker-compose logs -f frontend
docker-compose logs -f backend
docker-compose logs -f mysql
```

**Last 100 lines:**
```powershell
docker-compose logs --tail=100 backend
```

---

### Check Service Status

```powershell
docker-compose ps
```

**Expected output:**
```
NAME                    STATUS          PORTS
hackerhub-frontend     Up 2 minutes    0.0.0.0:9002->9002/tcp
hackerhub-backend      Up 2 minutes    0.0.0.0:8080->8080/tcp
hackerhub-mysql        Up 2 minutes    0.0.0.0:3306->3306/tcp
```

---

### Access Container Shell

```powershell
# Backend container
docker exec -it hackerhub-backend sh

# Frontend container
docker exec -it hackerhub-frontend sh

# MySQL container
docker exec -it hackerhub-mysql mysql -u hackerhub -p
# Password: hackerhub123
```

---

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the project root (optional):

```bash
# Copy the example
cp .env.example .env

# Edit values as needed
```

**Default values:**
```
MYSQL_ROOT_PASSWORD=hackerhub123
MYSQL_USER=hackerhub
MYSQL_PASSWORD=hackerhub123
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

### Change Ports

Edit `docker-compose.yml`:

```yaml
services:
  frontend:
    ports:
      - "3000:9002"  # Change left side (host port)
  
  backend:
    ports:
      - "8081:8080"  # Change left side (host port)
```

Then restart:
```powershell
docker-compose up -d
```

---

## 🧪 Testing the Setup

### 1. Check Health

```powershell
# Backend health
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

### 2. Test Frontend

1. Open browser: http://localhost:9002
2. Fill search form
3. Click "Scrape Now"
4. Results should appear!

### 3. Test Backend API Directly

```powershell
# Test MLH endpoint
$body = @{
    domain = "AI"
    location = "Online"
    count = 5
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/scraper/mlh" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

---

## 🐛 Troubleshooting

### Services Won't Start

```powershell
# Check what's using the ports
netstat -ano | findstr :8080
netstat -ano | findstr :9002
netstat -ano | findstr :3306

# Stop conflicting services
docker-compose down
```

---

### Backend Can't Connect to MySQL

**Solution 1:** Wait for MySQL to be ready
```powershell
# Check MySQL logs
docker-compose logs mysql

# Wait for: "ready for connections"
```

**Solution 2:** Restart backend after MySQL is ready
```powershell
docker-compose restart backend
```

---

### Frontend Can't Connect to Backend

**Check backend is running:**
```powershell
docker-compose ps backend
```

**Check logs:**
```powershell
docker-compose logs backend
```

**Verify API URL:**
```powershell
docker exec hackerhub-frontend env | grep NEXT_PUBLIC_API_URL
```

---

### Changes Not Reflecting

**Production mode:** Rebuild required
```powershell
docker-compose up -d --build frontend
```

**Development mode:** Should auto-reload
- If not working, restart the service:
```powershell
docker-compose -f docker-compose.dev.yml restart frontend
```

---

### Database Data Persistence

**View volumes:**
```powershell
docker volume ls | findstr hackerhub
```

**Backup database:**
```powershell
docker exec hackerhub-mysql mysqldump -u hackerhub -phackerhub123 hackhub > backup.sql
```

**Restore database:**
```powershell
docker exec -i hackerhub-mysql mysql -u hackerhub -phackerhub123 hackhub < backup.sql
```

---

## 🔄 Update Workflow

### Pulling Latest Changes

```powershell
# 1. Stop services
docker-compose down

# 2. Pull latest code
git pull

# 3. Rebuild and start
docker-compose up -d --build
```

---

### Clean Everything

**Remove all containers, networks, and volumes:**
```powershell
docker-compose down -v
docker system prune -a --volumes
```

**⚠️ Warning:** This deletes ALL Docker data including database!

---

## 📁 Docker Files Overview

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Production setup (optimized builds) |
| `docker-compose.dev.yml` | Development setup (hot reload) |
| `hackerhub_FE/Dockerfile` | Production frontend image |
| `hackerhub_FE/Dockerfile.dev` | Development frontend image |
| `hackhub_scraper_java/Dockerfile` | Backend image (multi-stage build) |
| `.env.example` | Environment variables template |

---

## 🎯 Common Commands Cheat Sheet

```powershell
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# Restart a service
docker-compose restart <service-name>

# Rebuild a service
docker-compose up -d --build <service-name>

# View logs
docker-compose logs -f <service-name>

# Check status
docker-compose ps

# Access container
docker exec -it <container-name> sh

# Development mode
docker-compose -f docker-compose.dev.yml up -d
```

---

## 🌐 Network Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Host (Windows)                 │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │         hackerhub-network (bridge)                  │ │
│  │                                                     │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │ │
│  │  │   MySQL      │  │   Backend    │  │ Frontend │ │ │
│  │  │   :3306      │◄─┤   :8080      │◄─┤  :9002   │ │ │
│  │  │              │  │              │  │          │ │ │
│  │  └──────────────┘  └──────────────┘  └──────────┘ │ │
│  └────────────────────────────────────────────────────┘ │
│           │                 │                 │          │
└───────────┼─────────────────┼─────────────────┼──────────┘
            │                 │                 │
            │                 │                 │
     localhost:3306    localhost:8080    localhost:9002
```

---

## ✅ Success Checklist

- [ ] Docker Desktop is installed and running
- [ ] Project is in `c:\Users\hp\Desktop\hackerhub\`
- [ ] Run `docker-compose up -d`
- [ ] Wait for services to start (~60 seconds)
- [ ] Frontend accessible at http://localhost:9002
- [ ] Backend accessible at http://localhost:8080
- [ ] Can search for hackathons successfully

---

## 🎉 You're All Set!

Your HackerHub application is now fully containerized!

**Next steps:**
1. Visit http://localhost:9002
2. Search for hackathons
3. Enjoy! 🚀

**Need help?** Check the troubleshooting section or run:
```powershell
docker-compose logs -f
```
