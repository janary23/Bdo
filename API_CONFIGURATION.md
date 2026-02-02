# 🔧 API Configuration Guide

## Current Status

Your Android app is configured but needs your InfinityFree domain URL.

## Option 1: Configure for InfinityFree (Production)

### Step 1: Get Your Domain

Your InfinityFree domain will be something like:
- `yourusername.infinityfreeapp.com`
- Or your custom domain if you added one

### Step 2: Update ApiService.kt

1. Open: `app/src/main/java/com/example/bdo/ApiService.kt`
2. Find line 89:
   ```kotlin
   private const val BASE_URL = "https://yourdomain.infinityfreeapp.com/api/"
   ```
3. Replace `yourdomain` with your actual domain
4. **Important:** Keep the `/api/` at the end!

**Example:**
```kotlin
private const val BASE_URL = "https://bdoloan.infinityfreeapp.com/api/"
```

### Step 3: Ensure HTTPS

InfinityFree provides free SSL certificates. Make sure to use `https://` not `http://`

---

## Option 2: Test Locally First (Recommended)

If you want to test before deploying to InfinityFree:

### Step 1: Install XAMPP/WAMP

1. Download XAMPP from https://www.apachefriends.org/
2. Install and start Apache and MySQL

### Step 2: Setup Local Database

1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. Create database: `bdo_loans`
3. Import: `admin/includes/database_schema.sql`

### Step 3: Copy Files to htdocs

```
C:\xampp\htdocs\
├── admin\      (copy entire admin folder)
└── api\        (copy entire api folder)
```

### Step 4: Update config.php for Local

Edit `admin/includes/config.php`:
```php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'bdo_loans');
```

### Step 5: Update ApiService.kt for Local

```kotlin
private const val BASE_URL = "http://10.0.2.2/api/"
```

**Note:** `10.0.2.2` is the special IP that Android emulator uses to access localhost

### Step 6: Test

1. Run Android app in emulator
2. Try registering a new user
3. Check if user appears in phpMyAdmin

---

## Quick Domain Finder

To find your InfinityFree domain:

1. Login to InfinityFree control panel
2. Look for "Website URL" or "Domain"
3. It will show your full domain

---

## What to Update

**File:** `app/src/main/java/com/example/bdo/ApiService.kt`  
**Line:** 89  
**Current:** `private const val BASE_URL = "https://yourdomain.infinityfreeapp.com/api/"`  
**Change to:** Your actual domain

---

## Testing Checklist

After updating the URL:

1. ✅ Sync Gradle in Android Studio
2. ✅ Run the app
3. ✅ Try to register a new user
4. ✅ Check if registration succeeds
5. ✅ Try to login
6. ✅ Try to apply for a loan
7. ✅ Check admin panel to see the data

---

## Common Issues

### Issue: "Network error: Unable to resolve host"
**Solution:** Check your BASE_URL is correct and device has internet

### Issue: "Failed to connect to yourdomain.infinityfreeapp.com"
**Solution:** 
- Verify domain is correct
- Check if you uploaded API files to InfinityFree
- Ensure you're using `https://` not `http://`

### Issue: "Connection refused"
**Solution:** 
- If testing locally, make sure XAMPP is running
- If using emulator, use `10.0.2.2` instead of `localhost`

---

## Need Help?

**What's your InfinityFree domain?**

Once you provide it, I can update the ApiService.kt file automatically for you!
