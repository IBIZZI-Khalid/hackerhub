# HackerHub Frontend - Local Setup Guide

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- **Node.js** 18.x or higher ([Download here](https://nodejs.org/))
- **npm** or **yarn** package manager
- **Backend server** (Spring Boot) running on `http://localhost:8080`

## 🚀 Quick Start

### 1. Install Dependencies

```bash
cd c:\Users\hp\Desktop\hackerhub\hackerhub_FE
npm install
```

This will install all required dependencies including:
- Next.js 15.5.9
- React 19.2.1
- TypeScript 5
- Tailwind CSS
- Radix UI components
- And other dependencies listed in `package.json`

### 2. Configure Environment Variables (Optional)

Create a `.env.local` file in the root directory if you want to use a different backend URL:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**Note**: The default backend URL is already set to `http://localhost:8080` in the code, so this step is optional for local development.

### 3. Start the Development Server

```bash
npm run dev
```

The frontend will start on: **http://localhost:9002**

### 4. Verify Backend Connection

Make sure your Spring Boot backend is running on port 8080:
- Navigate to `c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java`
- Run: `mvn spring-boot:run`

## 🔧 Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server on port 9002 with Turbopack |
| `npm run build` | Build production bundle |
| `npm run start` | Start production server |
| `npm run lint` | Run ESLint |
| `npm run typecheck` | Run TypeScript type checking |

## 📡 API Integration

The frontend is configured to connect to the Spring Boot backend:

### Backend Endpoints Used:
- **POST** `/api/scraper/mlh` - Scrape MLH hackathons
- **POST** `/api/scraper/devpost` - Scrape Devpost hackathons

### Request Format:
```json
{
  "title": "optional filter",
  "prize": "optional filter",
  "location": "optional filter",
  "count": 10
}
```

### Response Format:
```json
[
  {
    "id": 1,
    "title": "Hackathon Name",
    "description": "Full description",
    "blurb": "Short description",
    "url": "https://...",
    "location": "Online or City",
    "date": "2025-01-15",
    "imageUrl": "https://...",
    "provider": "MLH",
    "requirements": "Requirements text",
    "judges": "Judges info",
    "judgingCriteria": "Criteria",
    "type": "HACKATHON",
    "scrappedAt": "2025-12-27T01:00:00"
  }
]
```

## 🌐 Port Configuration

The frontend runs on **port 9002** (configured in `package.json`):
```json
"dev": "next dev --turbopack -p 9002"
```

To change the port, modify this line in `package.json`.

## 🔍 Troubleshooting

### Issue: "Cannot connect to backend"
**Solution**: 
1. Ensure Spring Boot backend is running: `mvn spring-boot:run` in the Java project
2. Verify MySQL database is running
3. Check if port 8080 is available

### Issue: "Dependencies missing"
**Solution**: Run `npm install` to install all dependencies

### Issue: "Port 9002 already in use"
**Solution**: 
1. Stop any process using port 9002
2. Or change the port in `package.json` script

### Issue: TypeScript errors
**Solution**: Run `npm run typecheck` to see all type errors. Most should resolve after running `npm install`.

## 📁 Project Structure

```
hackerhub_FE/
├── src/
│   ├── app/
│   │   ├── page.tsx        # Main homepage
│   │   ├── actions.ts       # Server actions for API calls
│   │   ├── layout.tsx       # Root layout
│   │   └── globals.css      # Global styles
│   ├── components/          # UI components
│   ├── lib/
│   │   ├── types.ts         # TypeScript types
│   │   ├── dummy-data.ts    # Mock data
│   │   └── utils.ts         # Utility functions
│   └── hooks/               # Custom React hooks
├── public/                  # Static assets
├── package.json             # Dependencies & scripts
├── next.config.ts           # Next.js configuration
├── tailwind.config.ts       # Tailwind CSS config
└── tsconfig.json            # TypeScript config
```

## ✨ Features

- 🔍 **Search & Filter**: Search for hackathons by title, prize, location
- 🎯 **Dual Provider Support**: Scrapes from both MLH and Devpost
- 📱 **Responsive Design**: Works on mobile, tablet, and desktop
- 🎨 **Modern UI**: Built with Tailwind CSS and Radix UI components
- ⚡ **Fast Loading**: Powered by Next.js 15 with Turbopack
- 🔄 **Real-time Feedback**: Loading states and error handling

## 🔗 Backend Requirements

The Spring Boot backend must be running with:
- MySQL database on `localhost:3306`
- Database name: `hackhub`
- Username: `root`
- Password: (empty or as configured in backend)

## 📝 Next Steps

1. Install dependencies: `npm install`
2. Start backend: Navigate to Java project and run `mvn spring-boot:run`
3. Start frontend: `npm run dev`
4. Open browser: `http://localhost:9002`
5. Test the connection by using the search form

## 🎉 You're Ready!

Once all dependencies are installed and both servers are running, you can:
- Search for hackathons using the search form
- Filter by platform (MLH/Devpost)
- View detailed hackathon information
- Navigate to event pages

For more information about the backend, see the documentation in `hackhub_scraper_java/README.md`.
