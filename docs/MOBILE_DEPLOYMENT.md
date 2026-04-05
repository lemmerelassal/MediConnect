# 📱 Mobile App Deployment Guide

## Option 1: PWA (Progressive Web App) - RECOMMENDED ⭐

### Why PWA?
- ✅ **$0 cost** - No app store fees
- ✅ **Instant updates** - No app review process
- ✅ **Single codebase** - Works everywhere
- ✅ **Offline support** - Service workers
- ✅ **Push notifications** - Just like native
- ✅ **Install to home screen** - Feels native
- ✅ **2-week timeline** - vs 4-6 weeks for native

### PWA Deployment Steps

#### Step 1: Add PWA Support (10 minutes)

```bash
cd frontend

# Install PWA schematic
ng add @angular/pwa

# This creates:
# - manifest.webmanifest (app metadata)
# - ngsw-config.json (service worker config)
# - Assets for icons
```

#### Step 2: Configure Manifest

Edit `src/manifest.webmanifest`:

```json
{
  "name": "Pharma Shortage Marketplace",
  "short_name": "PharmaShortage",
  "theme_color": "#667eea",
  "background_color": "#ffffff",
  "display": "standalone",
  "scope": "/",
  "start_url": "/",
  "icons": [
    {
      "src": "assets/icons/icon-72x72.png",
      "sizes": "72x72",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-96x96.png",
      "sizes": "96x96",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-128x128.png",
      "sizes": "128x128",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-144x144.png",
      "sizes": "144x144",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-152x152.png",
      "sizes": "152x152",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-384x384.png",
      "sizes": "384x384",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "maskable any"
    }
  ]
}
```

#### Step 3: Configure Service Worker

Edit `ngsw-config.json`:

```json
{
  "$schema": "./node_modules/@angular/service-worker/config/schema.json",
  "index": "/index.html",
  "assetGroups": [
    {
      "name": "app",
      "installMode": "prefetch",
      "resources": {
        "files": [
          "/favicon.ico",
          "/index.html",
          "/manifest.webmanifest",
          "/*.css",
          "/*.js"
        ]
      }
    },
    {
      "name": "assets",
      "installMode": "lazy",
      "updateMode": "prefetch",
      "resources": {
        "files": [
          "/assets/**",
          "/*.(svg|cur|jpg|jpeg|png|apng|webp|avif|gif)"
        ]
      }
    }
  ],
  "dataGroups": [
    {
      "name": "api",
      "urls": ["/api/**"],
      "cacheConfig": {
        "maxSize": 100,
        "maxAge": "1h",
        "strategy": "freshness"
      }
    }
  ]
}
```

#### Step 4: Add Install Prompt

Create `src/app/services/pwa.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Platform } from '@angular/cdk/platform';

@Injectable({
  providedIn: 'root'
})
export class PwaService {
  private deferredPrompt: any;

  constructor(private platform: Platform) {
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      this.deferredPrompt = e;
    });
  }

  public async installPwa(): Promise<void> {
    if (this.deferredPrompt) {
      this.deferredPrompt.prompt();
      const { outcome } = await this.deferredPrompt.userChoice;
      console.log(`User response: ${outcome}`);
      this.deferredPrompt = null;
    }
  }

  public canInstall(): boolean {
    return !!this.deferredPrompt;
  }
}
```

#### Step 5: Build & Deploy

```bash
# Build for production
npm run build -- --configuration production

# Deploy to Cloud Run (already configured)
# PWA features work automatically over HTTPS
```

#### Step 6: Test PWA

```bash
# Chrome DevTools
# 1. Open application in Chrome
# 2. F12 -> Application tab
# 3. Check "Service Workers"
# 4. Check "Manifest"
# 5. Run Lighthouse audit

# Test install prompt
# Look for "Install App" button in Chrome address bar
```

### PWA Features to Add

1. **Push Notifications** (already have backend support)
2. **Offline mode** (cache API responses)
3. **Background sync** (sync data when online)
4. **App shortcuts** (quick actions from home screen)

**TOTAL COST: $0 additional**

---

## Option 2: Capacitor Native Apps

### Why Capacitor?
- ✅ In App Store & Play Store
- ✅ Full native API access
- ✅ Better performance
- ❌ $124/year fees
- ❌ App review delays
- ❌ Need Mac for iOS

### Capacitor Deployment Steps

#### Step 1: Install Capacitor (15 minutes)

```bash
cd frontend

# Install Capacitor
npm install @capacitor/core @capacitor/cli
npm install @capacitor/android @capacitor/ios

# Initialize
npx cap init "Pharma Shortage" "com.pharma.shortage"

# Add platforms
npx cap add android
npx cap add ios
```

#### Step 2: Configure Android

```bash
# Build web assets
npm run build -- --configuration production

# Copy to Android
npx cap copy android

# Open in Android Studio
npx cap open android

# In Android Studio:
# 1. Update app/build.gradle with signing config
# 2. Create keystore for signing
# 3. Build -> Generate Signed Bundle/APK
```

**Create Signing Key:**
```bash
keytool -genkey -v -keystore pharma-release.keystore \
  -alias pharma -keyalg RSA -keysize 2048 -validity 10000
```

**Update `app/build.gradle`:**
```gradle
android {
    signingConfigs {
        release {
            storeFile file("../pharma-release.keystore")
            storePassword "your-password"
            keyAlias "pharma"
            keyPassword "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
```

#### Step 3: Configure iOS (Requires Mac)

```bash
# Copy to iOS
npx cap copy ios

# Open in Xcode
npx cap open ios

# In Xcode:
# 1. Select project
# 2. Signing & Capabilities
# 3. Add your Apple Developer account
# 4. Enable Push Notifications
# 5. Product -> Archive
# 6. Upload to App Store Connect
```

#### Step 4: Google Play Store Submission

**Prerequisites:**
- Google Play Developer account ($25 one-time)
- Privacy Policy URL
- App screenshots (at least 2)
- Feature graphic (1024x500)

**Steps:**
1. Go to https://play.google.com/console
2. Create application
3. Fill in store listing:
   - Title: Pharma Shortage Marketplace
   - Short description (80 chars)
   - Full description (4000 chars)
   - Screenshots (phone + tablet)
   - Feature graphic
   - App icon (512x512)
4. Set up pricing & distribution
5. Upload AAB file
6. Submit for review (1-3 days)

**Required Assets:**
```
Icon: 512x512 PNG
Feature Graphic: 1024x500 PNG
Screenshots: 
  - Phone: 16:9 or 9:16 (at least 2)
  - 7" Tablet: Same as phone (optional)
  - 10" Tablet: Same as phone (optional)
```

#### Step 5: Apple App Store Submission

**Prerequisites:**
- Apple Developer account ($99/year)
- Mac with Xcode
- App Store Connect account
- Privacy Policy URL

**Steps:**
1. Go to https://appstoreconnect.apple.com
2. Create new app
3. Fill in App Information:
   - Name: Pharma Shortage Marketplace
   - Primary language
   - Bundle ID: com.pharma.shortage
   - SKU: pharma-shortage-v1
4. Add app version
5. Screenshots (required for all device sizes):
   - iPhone 6.7" (1290x2796)
   - iPhone 6.5" (1242x2688)
   - iPhone 5.5" (1242x2208)
   - iPad Pro 12.9" (2048x2732)
6. Upload build via Xcode
7. Submit for review (2-7 days)

**Screenshot Sizes:**
```
iPhone 14 Pro Max: 1290x2796
iPhone 14 Pro: 1179x2556
iPhone 14 Plus: 1284x2778
iPhone 14: 1170x2532
iPad Pro 12.9": 2048x2732
iPad Pro 11": 1668x2388
```

#### Step 6: Continuous Deployment

Create GitHub Action for automated builds:

```yaml
# .github/workflows/mobile.yml
name: Mobile Build

on:
  push:
    branches: [main]

jobs:
  android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
      - run: npm install
      - run: npm run build
      - run: npx cap copy android
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - run: cd android && ./gradlew assembleRelease

  ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
      - run: npm install
      - run: npm run build
      - run: npx cap copy ios
      - run: cd ios/App && xcodebuild -scheme App -archivePath app.xcarchive archive
```

---

## Cost Comparison

### PWA
```
Development: Included
Hosting: Included in Cloud Run
Fees: $0/month
Maintenance: Minimal
Updates: Instant
─────────────────
TOTAL: $0/month
```

### Capacitor Native
```
Google Play: $25 one-time
Apple Developer: $99/year = $8.25/month
Mac for iOS: $79/month (MacStadium) OR $699 one-time
Development: +20-40 hours
Maintenance: +5-10 hours/month
Updates: 2-7 day review delay
─────────────────
TOTAL: $87/month (with cloud Mac)
OR $8.25/month (after buying Mac)
```

---

## Recommendation

### Start with PWA, then add native apps later if needed

**Timeline:**
- Week 1: Deploy PWA
- Week 2-3: Test with users
- Week 4+: If needed, add native apps

**Reasoning:**
1. PWA gets you to market faster
2. $0 cost to validate
3. Same functionality as native
4. Can always add native later
5. Users on both platforms can use it immediately

**When to add native apps:**
- Need specific native APIs (NFC, Bluetooth, etc.)
- Users expect app store presence
- Better marketing/discovery
- Enterprise deployment requirements

---

## Testing Checklist

### PWA Testing
- [ ] Install prompt appears
- [ ] App installs to home screen
- [ ] Works offline
- [ ] Push notifications work
- [ ] Lighthouse score >90
- [ ] Responsive on all devices

### Native App Testing
- [ ] APK/IPA installs correctly
- [ ] All features work
- [ ] Push notifications work
- [ ] Camera/file access works
- [ ] Offline mode works
- [ ] Performance acceptable
- [ ] No crashes

---

## Next Steps

1. **Deploy PWA first** (this weekend)
2. **Get user feedback** (2-4 weeks)
3. **Decide on native apps** based on feedback
4. **If yes to native:**
   - Register developer accounts
   - Create app icons & screenshots
   - Submit to stores
   - Wait for approval

**Estimated timeline: 2-6 weeks total**
