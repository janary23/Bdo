# 🚀 Quick Start Guide - BDO Loan Application

## Step 1: Import Database (REQUIRED)

### Option A: phpMyAdmin (Recommended)
1. Open: http://localhost/phpmyadmin/
2. Click "Import" tab
3. Choose file: `C:\Users\harry sevilla\AndroidStudioProjects\BDO\database\bdo_complete_schema.sql`
4. Click "Go"
5. ✅ Verify `bdo_loans` database with 13 tables exists

### Option B: MySQL Command Line
```bash
cd C:\xampp\mysql\bin
mysql -u root < "C:\Users\harry sevilla\AndroidStudioProjects\BDO\database\bdo_complete_schema.sql"
```

## Step 2: Test API Endpoints

Open in browser: http://localhost/bdo/api/test_all_endpoints.php

✅ All tests should pass with green checkmarks

## Step 3: Test Admin Panel

1. Open: http://localhost/bdo/admin/
2. Login:
   - Username: `admin`
   - Password: `admin123`
3. ✅ Dashboard should load

## Step 4: Test Android App

1. Make sure XAMPP is running (Apache + MySQL)
2. Run Android app in emulator
3. Test registration → login → apply for loan

## What's New

### ✨ 13 Database Tables
- `users`, `Client_Info`, `loan_applications`, `Loan_Application`
- `Loan_Records`, `loans`, `payments`, `Payment_Table`
- `Requirements_Table`, `Appointment_List`, `admin_users`
- `Notifications`, `Activity_Log`

### ✨ 10 New API Endpoints
- Dashboard stats
- User profile (get/update)
- Requirements (get/upload)
- Appointments (get/create)
- Notifications (get/mark read)

### ✨ Updated Android App
- New data models for Appointment & Notification
- All API endpoints integrated
- Ready to use all new features

## Files Created

**Database**:
- `database/bdo_complete_schema.sql` - Complete schema
- `database/setup_database.bat` - Setup script

**API** (10 new files):
- `api/get_dashboard_stats.php`
- `api/get_user_profile.php`
- `api/update_user_profile.php`
- `api/get_user_requirements.php`
- `api/upload_requirement.php`
- `api/get_appointments.php`
- `api/create_appointment.php`
- `api/get_notifications.php`
- `api/mark_notification_read.php`
- `api/test_all_endpoints.php`

**Documentation**:
- `DATABASE_SETUP.md` - Detailed setup guide
- `API_REFERENCE.md` - Complete API docs
- `QUICK_START.md` - This file

## Need Help?

📖 **Detailed Setup**: See `DATABASE_SETUP.md`  
📖 **API Documentation**: See `API_REFERENCE.md`  
📖 **Implementation Details**: See walkthrough artifact

## Troubleshooting

**Can't connect to database?**
- Check XAMPP MySQL is running
- Verify database imported successfully

**API not working?**
- Check XAMPP Apache is running
- Test: http://localhost/bdo/api/test_all_endpoints.php

**Android app can't connect?**
- Verify API URL is `http://10.0.2.2/bdo/api/`
- Check emulator can access localhost
