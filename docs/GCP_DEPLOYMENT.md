# Google Cloud Deployment - Step by Step

## Prerequisites

1. Google Cloud account with billing enabled
2. Install Google Cloud SDK: https://cloud.google.com/sdk/docs/install
3. Docker installed locally

## Initial Setup

```bash
# Login to Google Cloud
gcloud auth login

# Create new project
gcloud projects create pharma-shortage-prod --name="Pharma Shortage Marketplace"

# Set project
gcloud config set project pharma-shortage-prod

# Enable required APIs
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  storage.googleapis.com \
  cloudscheduler.googleapis.com \
  secretmanager.googleapis.com

# Set region
gcloud config set run/region us-central1
```

## Step 1: Cloud SQL (PostgreSQL)

```bash
# Create PostgreSQL instance (db-f1-micro = cheapest)
gcloud sql instances create pharma-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --root-password=YOUR_SECURE_PASSWORD

# Create database
gcloud sql databases create pharma_shortage --instance=pharma-db

# Get connection name
gcloud sql instances describe pharma-db --format='value(connectionName)'
# Save this - you'll need it!

# Import schema
gcloud sql import sql pharma-db gs://YOUR_BUCKET/schema.sql \
  --database=pharma_shortage
```

## Step 2: Cloud Storage (for uploaded documents)

```bash
# Create bucket for document uploads
gsutil mb -l us-central1 gs://pharma-shortage-docs

# Set lifecycle policy (optional - delete old files after 2 years)
cat > lifecycle.json << 'EOF'
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "Delete"},
        "condition": {"age": 730}
      }
    ]
  }
}
EOF

gsutil lifecycle set lifecycle.json gs://pharma-shortage-docs
```

## Step 3: Secrets Manager

```bash
# Store database password
echo -n "YOUR_DB_PASSWORD" | gcloud secrets create db-password --data-file=-

# Store JWT private key
cat backend/src/main/resources/META-INF/resources/privateKey.pem | \
  gcloud secrets create jwt-private-key --data-file=-

# Store SMTP credentials
echo -n "your-smtp-password" | gcloud secrets create smtp-password --data-file=-
```

## Step 4: Build Backend Docker Image

```bash
cd backend

# Build with Gradle
./gradlew build

# Build Docker image
docker build -t gcr.io/pharma-shortage-prod/backend:v1 .

# Push to Container Registry
docker push gcr.io/pharma-shortage-prod/backend:v1
```

## Step 5: Deploy Backend to Cloud Run

```bash
# Deploy backend
gcloud run deploy pharma-backend \
  --image gcr.io/pharma-shortage-prod/backend:v1 \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql:///pharma_shortage?cloudSqlInstance=YOUR_CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory" \
  --set-secrets "QUARKUS_DATASOURCE_PASSWORD=db-password:latest" \
  --set-secrets "SMTP_PASSWORD=smtp-password:latest" \
  --add-cloudsql-instances YOUR_CONNECTION_NAME \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --port 8080

# Get backend URL
gcloud run services describe pharma-backend --format='value(status.url)'
# Example: https://pharma-backend-abc123-uc.a.run.app
```

## Step 6: Build & Deploy Frontend

```bash
cd frontend

# Update API URL in environment
# Create src/environments/environment.prod.ts:
cat > src/environments/environment.prod.ts << 'EOF'
export const environment = {
  production: true,
  apiUrl: 'https://pharma-backend-abc123-uc.a.run.app/api'
};
EOF

# Build production
npm run build -- --configuration production

# Create Dockerfile for frontend
cat > Dockerfile.cloud << 'EOF'
FROM nginx:alpine
COPY dist/pharma-shortage-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
EOF

# Create nginx config
cat > nginx.conf << 'EOF'
server {
    listen 8080;
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
EOF

# Build and push
docker build -f Dockerfile.cloud -t gcr.io/pharma-shortage-prod/frontend:v1 .
docker push gcr.io/pharma-shortage-prod/frontend:v1

# Deploy frontend
gcloud run deploy pharma-frontend \
  --image gcr.io/pharma-shortage-prod/frontend:v1 \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 256Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --port 8080
```

## Step 7: Setup Custom Domain (Optional)

```bash
# Map custom domain
gcloud run domain-mappings create \
  --service pharma-frontend \
  --domain app.pharma-shortage.com

# Follow DNS instructions to add records
```

## Step 8: Setup Email Scheduler

```bash
# Create Cloud Scheduler job to process email queue
gcloud scheduler jobs create http email-processor \
  --schedule="*/5 * * * *" \
  --uri="https://pharma-backend-abc123-uc.a.run.app/api/email/process" \
  --http-method=POST
```

## Step 9: Monitoring & Logging

```bash
# Enable monitoring (included in free tier)
gcloud monitoring dashboards create

# Set up alerts
gcloud alpha monitoring policies create \
  --notification-channels=YOUR_CHANNEL \
  --display-name="High Error Rate" \
  --condition-display-name="Error rate > 5%" \
  --condition-threshold-value=0.05
```

## Step 10: Backup Strategy

```bash
# Automated daily backups
gcloud sql backups create --instance=pharma-db

# Set retention
gcloud sql instances patch pharma-db \
  --backup-start-time=03:00 \
  --retained-backups-count=30
```

## Testing Deployment

```bash
# Get frontend URL
FRONTEND_URL=$(gcloud run services describe pharma-frontend --format='value(status.url)')

# Test
curl $FRONTEND_URL
curl https://pharma-backend-abc123-uc.a.run.app/api/countries

# Open in browser
open $FRONTEND_URL
```

## Continuous Deployment with Cloud Build

Create `cloudbuild.yaml`:

```yaml
steps:
  # Build backend
  - name: 'gradle:jdk21'
    dir: 'backend'
    args: ['gradle', 'build']
  
  # Build backend image
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'gcr.io/$PROJECT_ID/backend:$SHORT_SHA', './backend']
  
  # Push backend
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'gcr.io/$PROJECT_ID/backend:$SHORT_SHA']
  
  # Deploy backend
  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    entrypoint: gcloud
    args:
      - 'run'
      - 'deploy'
      - 'pharma-backend'
      - '--image=gcr.io/$PROJECT_ID/backend:$SHORT_SHA'
      - '--region=us-central1'
      - '--platform=managed'

  # Similar steps for frontend...

timeout: '1200s'
```

## Scaling Configuration

```bash
# Auto-scale backend
gcloud run services update pharma-backend \
  --min-instances=1 \
  --max-instances=100 \
  --concurrency=80 \
  --cpu-throttling \
  --memory=1Gi

# Frontend can stay at min=0 (serverless)
```

## Security Hardening

```bash
# Enable VPC connector for private database access
gcloud compute networks vpc-access connectors create pharma-connector \
  --region=us-central1 \
  --range=10.8.0.0/28

# Update Cloud Run to use connector
gcloud run services update pharma-backend \
  --vpc-connector=pharma-connector

# Enable Cloud Armor for DDoS protection
gcloud compute security-policies create pharma-security-policy
```

## Cost Optimization

```bash
# Use committed use discounts for predictable workload
gcloud compute commitments create pharma-commitment \
  --resources=vcpu=2,memory=4 \
  --plan=12-month

# Schedule down-scaling for non-business hours
gcloud scheduler jobs create app-engine scale-down \
  --schedule="0 22 * * *" \
  --time-zone="America/New_York" \
  --http-method=POST \
  --uri="https://cloudresourcemanager.googleapis.com/v1/projects/pharma-shortage-prod:setIamPolicy" \
  --message-body='{"policy":{"bindings":[{"role":"roles/run.invoker","members":["allUsers"]}]}}'
```
