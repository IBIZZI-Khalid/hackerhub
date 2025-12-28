# 🚀 HackerHub - Quick Start Guide

## ✅ Status: Ready to Use!

Your HackerHub project is fully configured and ready to run.

---

## 📦 What Was Done

### Frontend Configuration ✅
1. ✅ Installed 957 npm packages
2. ✅ Fixed all 10 security vulnerabilities
3. ✅ Fixed missing `toast` function import
4. ✅ Configured backend API integration
5. ✅ Removed Turbopack (compatibility issue)
6. ✅ Server is running on **http://localhost:9002**

### Backend Status ℹ️
- Your Java Spring Boot backend is already working
- Needs to run on **http://localhost:8080**
- Uses MySQL database (`localhost:3306/hackhub`)

---

## 🎯 How to Start Everything

### Option 1: Quick Start (Both Servers)

**Terminal 1 - Backend:**
```powershell
cd c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java
mvn spring-boot:run
```

**Terminal 2 - Frontend (ALREADY RUNNING):**
```powershell
cd c:\Users\hp\Desktop\hackerhub\hackerhub_FE
npm run dev
```
✅ Frontend is already running on port 9002!

**Browser:**
```
http://localhost:9002
```

---

### Option 2: Start Backend Only (Frontend is Running)

Since the frontend is already running, you only need to start the backend:

```powershell
cd c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java
mvn spring-boot:run
```

Then open: **http://localhost:9002**

---

## 🧪 Testing the Connection

1. **Open your browser** → http://localhost:9002
2. **You should see** the HackerHub homepage with a search form
3. **Fill the search form**:
   - Scrape Type: Hackathons
   - Location: Online (or leave empty)
   - Count: 10
4. **Click "Find Hackathons"**
5. **Expected behavior**:
   - Loading skeletons appear
   - API calls to backend (check browser console F12)
   - Results display in a grid layout

### What to Check in Browser Console (F12):

```
[MLH] Calling API: http://localhost:8080/api/scraper/mlh
[DEVPOST] Calling API: http://localhost:8080/api/scraper/devpost
```

If you see these logs, the connection is working! ✅

---

## 🎨 Frontend Features

Your Next.js frontend includes:

- ✅ **Modern UI** with Tailwind CSS
- ✅ **Responsive design** (mobile, tablet, desktop)
- ✅ **Search & filters** for hackathons
- ✅ **Dual provider scraping** (MLH + Devpost simultaneously)
- ✅ **Real-time loading states**
- ✅ **Error handling with toast notifications**
- ✅ **Event grid display**
- ✅ **TypeScript** for type safety
- ✅ **Server Actions** for API calls

---

## ⚙️ Configuration Details

### Ports:
- **Frontend**: 9002
- **Backend**: 8080
- **MySQL**: 3306

### Backend API Endpoints:
- `POST http://localhost:8080/api/scraper/mlh`
- `POST http://localhost:8080/api/scraper/devpost`

### Environment Variables:
The frontend automatically connects to `http://localhost:8080`. To change this, create a `.env.local` file:

```bash
NEXT_PUBLIC_API_URL=http://your-custom-url
```

---

## 🐛 Troubleshooting

### Problem: "Cannot connect to backend"

**Check if backend is running:**
```powershell
curl http://localhost:8080
```

**If not, start it:**
```powershell
cd c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java
mvn spring-boot:run
```

---

### Problem: "MySQL connection error"

1. **Start MySQL service**
2. **Create database:**
   ```sql
   CREATE DATABASE IF NOT EXISTS hackhub;
   ```
3. **Check credentials** in `hackhub_scraper_java/src/main/resources/application.properties`

---

### Problem: "Port 9002 already in use"

**Stop the current process and restart:**
```powershell
# Press Ctrl+C in the terminal running the dev server
# Then start again:
npm run dev
```

**Or change the port** in `package.json`:
```json
"dev": "next dev -p 9003"  // Use different port
```

---

## 📁 Project Structure

```
c:\Users\hp\Desktop\hackerhub\
│
├── hackerhub_FE/                      ← FRONTEND (Next.js + React)
│   ├── src/
│   │   ├── app/
│   │   │   ├── page.tsx              ✅ Fixed (added toast)
│   │   │   ├── actions.ts            ✅ Fixed (API integration)
│   │   │   └── ...
│   │   ├── components/               (UI components)
│   │   └── lib/                      (Types, utils, data)
│   ├── node_modules/                  ✅ Installed (957 packages)
│   ├── package.json                   ✅ Updated (removed --turbopack)
│   ├── SETUP.md                       📄 Full setup guide
│   ├── BACKEND_INTEGRATION.md         📄 API integration guide
│   └── CONFIGURATION_SUMMARY.md       📄 All changes detailed
│
└── hackhub_scraper_java/              ← BACKEND (Spring Boot + Java)
    ├── src/main/java/                 (Java source code)
    ├── src/main/resources/
    │   └── application.properties     (MySQL config)
    ├── pom.xml                        (Maven dependencies)
    └── README.md                      (Backend docs)
```

---

## 📚 Documentation Files

| File | Description |
|------|-------------|
| `SETUP.md` | Complete local development setup |
| `BACKEND_INTEGRATION.md` | How frontend connects to backend |
| `CONFIGURATION_SUMMARY.md` | All changes made during configuration |
| `QUICKSTART.md` (this file) | Fast track to get started |

---

## 💡 Pro Tips

1. **Keep both terminals open** - one for frontend, one for backend
2. **Check browser console** (F12) to see API calls in real-time
3. **Hot reload enabled** - changes to code auto-refresh
4. **TypeScript errors** - VSCode will show them inline
5. **Backend logs** - Monitor Spring Boot terminal for scraping progress

---

## 🎉 You're Ready!

### Current Status:
- ✅ Frontend: **RUNNING** on http://localhost:9002
- ⏳ Backend: **Waiting** (start with `mvn spring-boot:run`)
- ⏳ MySQL: **Required** (must be running)

### Next Actions:
1. ✅ Frontend is ready (already running)
2. 🚀 Start the backend server
3. 🌐 Open http://localhost:9002
4. 🔍 Start searching for hackathons!

---

**Configured on**: December 27, 2025 at 02:12 AM  
**Frontend Port**: 9002  
**Backend Port**: 8080  
**Status**: ✅ Ready for Development

---

## 🆘 Need Help?

- **Frontend issues**: Check `SETUP.md`
- **Backend connection**: Check `BACKEND_INTEGRATION.md`
- **Full details**: Check `CONFIGURATION_SUMMARY.md`
- **Backend docs**: Check `hackhub_scraper_java/README.md`

Happy hacking! 🎯
