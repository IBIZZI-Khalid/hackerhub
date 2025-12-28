# Backend Integration Configuration

This document explains how the frontend connects to the Spring Boot backend.

## Environment Variables

The frontend uses the following environment variable to connect to the backend:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### How to Configure

1. **Create a `.env.local` file** in the root directory (optional):
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:8080
   ```

2. **Default Behavior**: If no environment variable is set, the application automatically uses `http://localhost:8080`

### For Different Environments

- **Local Development**: `http://localhost:8080` (default)
- **Production**: Update `.env.local` with your production API URL
- **Staging**: Update `.env.local` with your staging API URL

## Backend Configuration Checklist

Before running the frontend, ensure:

✅ **MySQL Database** is running on `localhost:3306`
   - Database name: `hackhub`
   - Username: `root`
   - Password: `` (empty, or as configured)

✅ **Spring Boot Backend** is running on port `8080`
   - Navigate to: `c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java`
   - Run: `mvn spring-boot:run`
   - Verify at: `http://localhost:8080`

✅ **Backend Endpoints** are accessible:
   - `POST http://localhost:8080/api/scraper/mlh`
   - `POST http://localhost:8080/api/scraper/devpost`

## Connection Flow

```
Frontend (Next.js on :9002)
    ↓
    → API Request (POST)
    ↓
Backend (Spring Boot on :8080)
    ↓
    → /api/scraper/mlh or /api/scraper/devpost
    ↓
Scraper Services (MLH/Devpost)
    ↓
MySQL Database (:3306/hackhub)
    ↓
Response back to Frontend
```

## API Request Format

The frontend sends requests in this format:

```typescript
{
  title?: string;      // Optional filter
  prize?: string;      // Optional filter
  location?: string;   // Optional filter
  count: number;       // Number of results (5-50)
}
```

Example:
```json
{
  "title": "",
  "prize": "",
  "location": "Online",
  "count": 10
}
```

## API Response Format

The backend returns an array of events:

```typescript
[
  {
    "id": 1,
    "title": "MLH Hackathon 2025",
    "description": "Full description...",
    "blurb": "Short summary",
    "url": "https://hackathon.com",
    "location": "Online",
    "date": "2025-02-15",
    "imageUrl": "https://...",
    "provider": "MLH",
    "requirements": "Requirements text",
    "judges": "Judge information",
    "judgingCriteria": "Criteria details",
    "type": "HACKATHON",
    "scrappedAt": "2025-12-27T02:00:00"
  }
]
```

## Error Handling

The frontend handles the following errors:

1. **Connection Failed**: 
   - Message: "Could not connect to the backend service"
   - Action: Check if Spring Boot server is running

2. **HTTP Error (4xx/5xx)**:
   - Message: "Failed to scrape from [PROVIDER]"
   - Action: Check backend logs

3. **No Results**:
   - Message: "No Results Found"
   - Action: Adjust search filters

## CORS Configuration

For production deployment, ensure your Spring Boot backend has CORS configured:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:9002", "https://your-frontend-domain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## Testing the Connection

1. Start the backend:
   ```bash
   cd c:\Users\hp\Desktop\hackerhub\hackhub_scraper_java
   mvn spring-boot:run
   ```

2. Start the frontend:
   ```bash
   cd c:\Users\hp\Desktop\hackerhub\hackerhub_FE
   npm run dev
   ```

3. Open browser: `http://localhost:9002`

4. Use the search form to test the connection

5. Check browser console for API logs:
   ```
   [MLH] Calling API: http://localhost:8080/api/scraper/mlh
   [DEVPOST] Calling API: http://localhost:8080/api/scraper/devpost
   ```

## Troubleshooting

### Issue: "Could not connect to the backend service"

**Causes:**
- Backend server is not running
- Backend is running on a different port
- Firewall blocking connection

**Solutions:**
1. Verify backend is running: `curl http://localhost:8080/api/scraper/mlh`
2. Check Spring Boot application logs
3. Ensure MySQL is running

### Issue: "Failed to scrape from MLH/DEVPOST"

**Causes:**
- Backend encountered an error during scraping
- Database connection issue
- External API (MLH/Devpost) is down

**Solutions:**
1. Check Spring Boot application logs
2. Verify MySQL connection in `application.properties`
3. Test scraping endpoints directly using Postman

### Issue: Frontend not calling backend

**Causes:**
- Environment variable misconfigured
- Code not fetching from correct URL

**Solutions:**
1. Check browser console for API call logs
2. Verify `NEXT_PUBLIC_API_URL` in `.env.local` (if used)
3. Check network tab in browser DevTools

## Security Considerations

For production:

1. ✅ Use HTTPS for all API calls
2. ✅ Configure proper CORS policies
3. ✅ Add authentication/authorization
4. ✅ Rate limiting on backend
5. ✅ Input validation on both frontend and backend
6. ✅ Secure environment variables
