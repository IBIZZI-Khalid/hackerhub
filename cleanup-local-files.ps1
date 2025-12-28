# Clean up script for Docker-only development
# Run this to remove local build artifacts and dependencies

Write-Host "🧹 Cleaning up local development files..." -ForegroundColor Cyan

# Frontend cleanup
Write-Host "`n📦 Removing frontend node_modules..." -ForegroundColor Yellow
if (Test-Path "hackerhub_FE\node_modules") {
    Remove-Item -Path "hackerhub_FE\node_modules" -Recurse -Force
    Write-Host "✅ Removed frontend node_modules" -ForegroundColor Green
}

Write-Host "`n📦 Removing frontend .next build..." -ForegroundColor Yellow
if (Test-Path "hackerhub_FE\.next") {
    Remove-Item -Path "hackerhub_FE\.next" -Recurse -Force
    Write-Host "✅ Removed frontend .next folder" -ForegroundColor Green
}

# Backend cleanup
Write-Host "`n📦 Removing backend target folder..." -ForegroundColor Yellow
if (Test-Path "hackhub_scraper_java\target") {
    Remove-Item -Path "hackhub_scraper_java\target" -Recurse -Force
    Write-Host "✅ Removed backend target folder" -ForegroundColor Green
}

# Remove logs
Write-Host "`n📝 Removing log files..." -ForegroundColor Yellow
Get-ChildItem -Path . -Include *.log -Recurse | Remove-Item -Force
Write-Host "✅ Removed log files" -ForegroundColor Green

# Calculate space saved
Write-Host "`n💾 Checking disk space..." -ForegroundColor Cyan
$size = (Get-ChildItem -Path . -Recurse -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum / 1GB
Write-Host "Current project size: $([math]::Round($size, 2)) GB" -ForegroundColor White

Write-Host "`n✨ Cleanup complete! You're now running Docker-only." -ForegroundColor Green
Write-Host "`n📚 Files you can safely delete:" -ForegroundColor Cyan
Write-Host "  ❌ node_modules (handled by Docker)" -ForegroundColor Gray
Write-Host "  ❌ .next (handled by Docker)" -ForegroundColor Gray
Write-Host "  ❌ target (handled by Docker)" -ForegroundColor Gray
Write-Host "  ❌ *.log files" -ForegroundColor Gray

Write-Host "`n📂 Files to KEEP:" -ForegroundColor Cyan
Write-Host "  ✅ All source code (src/)" -ForegroundColor Gray
Write-Host "  ✅ Configuration files (*.json, *.ts, *.yml)" -ForegroundColor Gray
Write-Host "  ✅ Docker files (Dockerfile, docker-compose.yml)" -ForegroundColor Gray
Write-Host "  ✅ Documentation (*.md)" -ForegroundColor Gray
