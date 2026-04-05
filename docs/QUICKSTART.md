# 🚀 Quick Start Guide

## Docker Setup (Easiest - 5 minutes)

```bash
docker-compose up -d
# Frontend: http://localhost:4200
# Login: admin@pharma.org / Admin123!
```

## Manual Setup

### 1. Database
```bash
createdb pharma_shortage
psql -d pharma_shortage -f schema.sql
```

### 2. JWT Keys
```bash
cd backend/src/main/resources/META-INF/resources
openssl genrsa -out privateKey.pem 2048
openssl rsa -pubout -in privateKey.pem -out publicKey.pem
```

### 3. Backend
```bash
cd backend
./gradlew quarkusDev
```

### 4. Frontend
```bash
cd frontend
npm install
npm start
```

**Access:** http://localhost:4200  
**Login:** admin@pharma.org / Admin123!
