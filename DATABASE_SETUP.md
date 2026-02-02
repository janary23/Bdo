# BDO Loan Application - Database Setup Guide

## Overview

This guide will help you set up the complete database schema for the BDO Loan Application system. The database includes 13 tables that support both the Android app and the admin web panel.

## Prerequisites

- **XAMPP** installed (with Apache and MySQL)
- **phpMyAdmin** access (http://localhost/phpmyadmin/)
- Basic knowledge of running batch scripts

## Quick Setup (Recommended)

### Option 1: Automated Setup Script

1. **Start XAMPP**
   - Open XAMPP Control Panel
   - Start **Apache** and **MySQL**

2. **Run the Setup Script**
   ```bash
   cd C:\Users\harry sevilla\AndroidStudioProjects\BDO\database
   setup_database.bat
   ```

3. **Verify Success**
   - The script will display "SUCCESS! Database setup complete!"
   - Default admin credentials will be shown

### Option 2: Manual Setup via phpMyAdmin

1. **Open phpMyAdmin**
   - Navigate to http://localhost/phpmyadmin/

2. **Import SQL File**
   - Click on "Import" tab
   - Click "Choose File"
   - Select: `C:\Users\harry sevilla\AndroidStudioProjects\BDO\database\bdo_complete_schema.sql`
   - Click "Go"

3. **Verify Database**
   - Check that `bdo_loans` database is created
   - Verify all 13 tables exist

## Database Schema

### Tables Overview

| Table Name | Purpose | Records |
|------------|---------|---------|
| `users` | Android app users (base table) | User accounts |
| `Client_Info` | Staff module compatibility | Extended client info |
| `loan_applications` | Dashboard compatibility | Loan applications |
| `Loan_Application` | Staff module | Loan applications (PascalCase) |
| `Loan_Records` | Approved loans | Loan records |
| `loans` | Dashboard compatibility | Active loans |
| `payments` | Android API compatibility | Payment records |
| `Payment_Table` | Staff module | Payment records (PascalCase) |
| `Requirements_Table` | Document uploads | User documents |
| `Appointment_List` | Appointment scheduling | User appointments |
| `admin_users` | Admin login | Admin accounts |
| `Notifications` | User notifications | System notifications |
| `Activity_Log` | System activity | Audit trail |

### Table Relationships

```
users (base table)
├── Client_Info (1:1)
├── loan_applications (1:many)
├── Loan_Application (1:many)
├── loans (1:many)
├── payments (1:many)
├── Requirements_Table (1:many)
├── Appointment_List (1:many)
└── Notifications (1:many)

Loan_Application
├── Loan_Records (1:1)
├── Payment_Table (1:many)
└── Appointment_List (1:many)

loans
└── payments (1:many)
```

## Default Credentials

### Admin Panel Access

- **URL**: http://localhost/bdo/admin/
- **Username**: `admin`
- **Password**: `admin123`

> ⚠️ **IMPORTANT**: Change the default admin password after first login!

## Database Configuration

The database connection is configured in:
```
C:\Users\harry sevilla\AndroidStudioProjects\BDO\admin\includes\config.php
```

Current settings (XAMPP local):
```php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'bdo_loans');
```

## Verification Steps

### 1. Check Database Creation

```sql
SHOW DATABASES LIKE 'bdo_loans';
```

### 2. Verify All Tables

```sql
USE bdo_loans;
SHOW TABLES;
```

Expected output: 13 tables

### 3. Check Admin User

```sql
SELECT username, full_name, role FROM admin_users;
```

Expected: 1 admin user

### 4. Test API Connection

Navigate to: http://localhost/bdo/api/test_all_endpoints.php

This will test all API endpoints and show results.

## Troubleshooting

### Error: "MySQL is not running"

**Solution**: 
1. Open XAMPP Control Panel
2. Click "Start" next to MySQL
3. Wait for it to turn green
4. Run the setup script again

### Error: "Access denied for user 'root'"

**Solution**:
1. Check if you've set a MySQL root password
2. Update `config.php` with your password
3. Update `setup_database.bat` with your password

### Error: "Database already exists"

**Solution**:
The script will automatically drop and recreate the database. If you want to keep existing data, backup first:

```bash
# Backup existing database
mysqldump -u root bdo_loans > backup.sql

# Restore if needed
mysql -u root bdo_loans < backup.sql
```

### Error: "Table doesn't exist" when using app

**Solution**:
1. Verify all 13 tables exist in phpMyAdmin
2. Re-run the setup script
3. Clear Android app cache and data

## Database Maintenance

### Backup Database

```bash
cd C:\xampp\mysql\bin
mysqldump -u root bdo_loans > C:\backup\bdo_loans_backup.sql
```

### Restore Database

```bash
cd C:\xampp\mysql\bin
mysql -u root bdo_loans < C:\backup\bdo_loans_backup.sql
```

### Reset Database

Simply run the setup script again:
```bash
cd C:\Users\harry sevilla\AndroidStudioProjects\BDO\database
setup_database.bat
```

## Next Steps

After successful database setup:

1. ✅ Test the admin panel: http://localhost/bdo/admin/
2. ✅ Test the API endpoints: http://localhost/bdo/api/test_all_endpoints.php
3. ✅ Run the Android app and test registration/login
4. ✅ Test loan application flow
5. ✅ Verify data appears in phpMyAdmin

## Support

If you encounter any issues:

1. Check XAMPP error logs: `C:\xampp\mysql\data\mysql_error.log`
2. Check Apache error logs: `C:\xampp\apache\logs\error.log`
3. Verify PHP version is 7.4 or higher
4. Ensure port 3306 (MySQL) is not blocked by firewall
