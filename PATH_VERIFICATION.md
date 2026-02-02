# ✅ Path Verification & Testing Guide

## 📁 Correct Folder Structure on InfinityFree

Your `htdocs` folder should look like this:

```
htdocs/
├── admin/
│   ├── admin_login.php
│   ├── admin_dashboard.php
│   ├── manage_applications.php
│   ├── manage_users.php
│   ├── view_payments.php
│   ├── reports.php
│   ├── logout.php
│   ├── index.php
│   ├── css/
│   │   └── admin_styles.css
│   └── includes/
│       ├── config.php
│       └── header.php
└── api/
    ├── login.php
    ├── register.php
    ├── apply_loan.php
    ├── get_user_loans.php
    ├── get_applications.php
    └── get_payments.php
```

## ✅ All Paths Are Correct!

I've verified all file paths and they're configured correctly:

### Admin Panel Paths ✅
- `admin_login.php` → redirects to `admin_dashboard.php` ✅
- `index.php` → redirects to `admin_login.php` ✅
- `logout.php` → redirects to `admin_login.php` ✅
- All files include: `includes/config.php` ✅

### API Paths ✅
- All API files include: `../admin/includes/config.php` ✅
- This is correct because API is in `htdocs/api/` and config is in `htdocs/admin/includes/`

## 🧪 Testing Your Setup

### Test 1: Admin Login ✅
**URL:** https://adminbdo.ct.ws/admin/admin_login.php  
**Status:** WORKING (you confirmed this)

**Credentials:**
- Username: `admin`
- Password: `admin123`

### Test 2: Admin Dashboard
**URL:** https://adminbdo.ct.ws/admin/admin_dashboard.php  
**Expected:** Should redirect to login if not logged in, or show dashboard if logged in

### Test 3: API Endpoint - Login
**URL:** https://adminbdo.ct.ws/api/login.php  
**Method:** POST  
**Test with browser:**
```
https://adminbdo.ct.ws/api/login.php
```
**Expected:** JSON response saying "Invalid request method. Use POST."

### Test 4: API Endpoint - Get Loans
**URL:** https://adminbdo.ct.ws/api/get_user_loans.php?user_id=1  
**Method:** GET  
**Test with browser:**
```
https://adminbdo.ct.ws/api/get_user_loans.php?user_id=1
```
**Expected:** JSON response with loan data

## 🔧 Quick Tests You Can Do Now

### 1. Test Admin Panel
1. Go to: https://adminbdo.ct.ws/admin/
2. Should redirect to: https://adminbdo.ct.ws/admin/admin_login.php
3. Login with: `admin` / `admin123`
4. Should redirect to: https://adminbdo.ct.ws/admin/admin_dashboard.php

### 2. Test API in Browser
Open these URLs in your browser:

**Get User Loans:**
```
https://adminbdo.ct.ws/api/get_user_loans.php?user_id=1
```
Expected response:
```json
{
  "success": true,
  "loans": [...]
}
```

**Get Applications:**
```
https://adminbdo.ct.ws/api/get_applications.php?user_id=1
```

**Get Payments:**
```
https://adminbdo.ct.ws/api/get_payments.php?user_id=1
```

### 3. Test Android App
1. Open Android Studio
2. Sync Gradle: `File → Sync Project with Gradle Files`
3. Run the app
4. Try to register a new user
5. Try to login
6. Try to apply for a loan

## 🐛 Common Issues & Solutions

### Issue: "Database connection error"
**Cause:** config.php can't connect to database  
**Solution:** 
- Check credentials in `admin/includes/config.php`
- Verify database exists in phpMyAdmin

### Issue: "404 Not Found" on admin pages
**Cause:** Files not uploaded correctly  
**Solution:**
- Verify `admin/` folder is in `htdocs/admin/`
- Check file names are correct (case-sensitive on Linux servers)

### Issue: "404 Not Found" on API endpoints
**Cause:** API files not uploaded  
**Solution:**
- Verify `api/` folder is in `htdocs/api/`
- Check all 6 PHP files are present

### Issue: "Failed to include config.php"
**Cause:** Path issue in API files  
**Solution:**
- Verify folder structure matches above
- API files should use: `../admin/includes/config.php`

### Issue: Android app shows "Network error"
**Cause:** API URL incorrect or API not working  
**Solution:**
- Verify `ApiService.kt` has: `https://adminbdo.ct.ws/api/`
- Test API endpoints in browser first
- Check device has internet connection

## 📊 Expected Behavior

### After Successful Login:
1. ✅ Redirects to dashboard
2. ✅ Shows statistics (applications, loans, payments)
3. ✅ Navigation menu works
4. ✅ Can view all pages

### After Android App Registration:
1. ✅ User appears in admin panel → Manage Users
2. ✅ Can login with new credentials
3. ✅ Can apply for loans
4. ✅ Applications appear in admin panel

## 🎯 Next Steps

1. **Login to Admin Panel**
   - URL: https://adminbdo.ct.ws/admin/
   - Credentials: `admin` / `admin123`

2. **Test All Admin Features**
   - Dashboard
   - Manage Applications
   - Manage Users
   - View Payments
   - Reports

3. **Test API Endpoints**
   - Open URLs in browser
   - Verify JSON responses

4. **Test Android App**
   - Register new user
   - Login
   - Apply for loan
   - View loans

5. **Change Admin Password**
   - ⚠️ IMPORTANT: Change default password!

## ✅ Everything Should Work!

All paths are verified and correct. Your system is ready to use!

**Admin Panel:** https://adminbdo.ct.ws/admin/  
**API:** https://adminbdo.ct.ws/api/  
**Database:** Connected and working ✅
