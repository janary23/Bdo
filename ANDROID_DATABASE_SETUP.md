# Android App Database Integration Guide

This guide shows you how to connect your BDO Android app to the InfinityFree MySQL database.

## 🔐 Your Database Credentials

Based on your InfinityFree account:

```
MySQL Hostname:   sql211.infinityfree.com
MySQL Port:       3306
MySQL Username:   if0_41042307
MySQL Password:   6FASKXmhvN9JxTc
Database Name:    if0_41042307_bdo
```

## 🚀 Two Integration Approaches

### Option 1: REST API (Recommended) ⭐

This is the **recommended approach** for production apps because:
- ✅ More secure (database credentials not in app)
- ✅ Better performance
- ✅ Easier to maintain
- ✅ Works with InfinityFree's limitations

### Option 2: Direct MySQL Connection

This approach connects directly to MySQL but has limitations:
- ⚠️ Less secure (credentials in app)
- ⚠️ May not work on all networks
- ⚠️ InfinityFree may block direct connections
- ✅ Good for testing/development

---

## 📱 Option 1: REST API Integration (Recommended)

### Step 1: Create API Endpoints

Create these PHP files in your InfinityFree hosting under `htdocs/api/`:

#### 1.1 Create `api/login.php`

```php
<?php
require_once '../admin/includes/config.php';
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = clean_input($_POST['email']);
    $password = $_POST['password'];
    
    $query = "SELECT * FROM users WHERE email = '$email' AND status = 'active' LIMIT 1";
    $result = mysqli_query($conn, $query);
    
    if ($result && mysqli_num_rows($result) == 1) {
        $user = mysqli_fetch_assoc($result);
        
        if (password_verify($password, $user['password'])) {
            unset($user['password']); // Don't send password back
            echo json_encode([
                'success' => true,
                'message' => 'Login successful',
                'user' => $user
            ]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Invalid credentials']);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'User not found']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
}
?>
```

#### 1.2 Create `api/apply_loan.php`

```php
<?php
require_once '../admin/includes/config.php';
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = clean_input($_POST['user_id']);
    $loan_type = clean_input($_POST['loan_type']);
    $amount = clean_input($_POST['amount']);
    $term_months = clean_input($_POST['term_months']);
    $monthly_payment = clean_input($_POST['monthly_payment']);
    
    $query = "INSERT INTO loan_applications (user_id, loan_type, amount, term_months, monthly_payment) 
              VALUES ($user_id, '$loan_type', $amount, $term_months, $monthly_payment)";
    
    if (mysqli_query($conn, $query)) {
        echo json_encode([
            'success' => true,
            'message' => 'Application submitted successfully',
            'application_id' => mysqli_insert_id($conn)
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error submitting application']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
}
?>
```

#### 1.3 Create `api/get_user_loans.php`

```php
<?php
require_once '../admin/includes/config.php';
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $user_id = clean_input($_GET['user_id']);
    
    $query = "SELECT * FROM loans WHERE user_id = $user_id ORDER BY created_at DESC";
    $result = mysqli_query($conn, $query);
    
    $loans = [];
    while ($row = mysqli_fetch_assoc($result)) {
        $loans[] = $row;
    }
    
    echo json_encode([
        'success' => true,
        'loans' => $loans
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
}
?>
```

### Step 2: Add Dependencies to Android App

Add these to your `app/build.gradle`:

```gradle
dependencies {
    // Existing dependencies...
    
    // For HTTP requests
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // Coroutines for async operations
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### Step 3: Create API Service in Android

Create `ApiService.kt`:

```kotlin
package com.example.bdo

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Data models
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String, val user: User?)
data class User(
    val user_id: Int,
    val email: String,
    val full_name: String,
    val phone: String?,
    val address: String?,
    val status: String
)

data class LoanApplicationRequest(
    val user_id: Int,
    val loan_type: String,
    val amount: Double,
    val term_months: Int,
    val monthly_payment: Double
)

data class LoanApplicationResponse(
    val success: Boolean,
    val message: String,
    val application_id: Int?
)

data class LoansResponse(val success: Boolean, val loans: List<Loan>)
data class Loan(
    val loan_id: Int,
    val loan_type: String,
    val principal_amount: Double,
    val term_months: Int,
    val monthly_payment: Double,
    val total_paid: Double,
    val remaining_balance: Double,
    val status: String
)

// API Interface
interface ApiService {
    
    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @FormUrlEncoded
    @POST("apply_loan.php")
    suspend fun applyLoan(
        @Field("user_id") userId: Int,
        @Field("loan_type") loanType: String,
        @Field("amount") amount: Double,
        @Field("term_months") termMonths: Int,
        @Field("monthly_payment") monthlyPayment: Double
    ): Response<LoanApplicationResponse>
    
    @GET("get_user_loans.php")
    suspend fun getUserLoans(@Query("user_id") userId: Int): Response<LoansResponse>
}

// API Client
object ApiClient {
    private const val BASE_URL = "https://yourdomain.infinityfreeapp.com/api/"
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

### Step 4: Update LoginActivity to Use API

```kotlin
// In LoginActivity.kt
import kotlinx.coroutines.*

private fun attemptLogin() {
    val email = emailEditText.text.toString().trim()
    val password = passwordEditText.text.toString().trim()
    
    // Validation...
    
    // Make API call
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.apiService.login(email, password)
            
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    
                    // Save user data to SharedPreferences
                    val prefs = getSharedPreferences("BDO_PREFS", MODE_PRIVATE)
                    prefs.edit().apply {
                        putInt("user_id", user?.user_id ?: 0)
                        putString("user_name", user?.full_name)
                        putString("user_email", user?.email)
                        apply()
                    }
                    
                    // Navigate to dashboard
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, 
                        response.body()?.message ?: "Login failed", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, 
                    "Network error: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### Step 5: Update ApplyFragment to Submit Loans

```kotlin
// In ApplyFragment.kt
private fun setupSubmit(view: View) {
    btnSubmit.setOnClickListener {
        val prefs = requireActivity().getSharedPreferences("BDO_PREFS", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        
        val amount = amountSlider.value.toDouble()
        val termMonths = termSlider.value.toInt()
        val monthlyPayment = amount / termMonths
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.applyLoan(
                    userId = userId,
                    loanType = selectedType,
                    amount = amount,
                    termMonths = termMonths,
                    monthlyPayment = monthlyPayment
                )
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, 
                            "Application submitted successfully!", 
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, 
                            "Error submitting application", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, 
                        "Network error: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
```

---

## 🔧 Option 2: Direct MySQL Connection (Alternative)

> ⚠️ **Warning**: This approach may not work with InfinityFree's free hosting due to remote connection restrictions.

### Step 1: Add MySQL JDBC Driver

Add to `app/build.gradle`:

```gradle
dependencies {
    implementation 'mysql:mysql-connector-java:8.0.33'
}
```

### Step 2: Add Internet Permission

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Step 3: Create Database Helper

The `DatabaseConfig.kt` file has already been created with your credentials.

### Step 4: Create Database Manager

```kotlin
package com.example.bdo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager

class DatabaseManager {
    
    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun loginUser(email: String, password: String): User? = withContext(Dispatchers.IO) {
        val connection = getConnection()
        try {
            val statement = connection?.prepareStatement(
                "SELECT * FROM users WHERE email = ? AND status = 'active' LIMIT 1"
            )
            statement?.setString(1, email)
            val resultSet = statement?.executeQuery()
            
            if (resultSet?.next() == true) {
                User(
                    user_id = resultSet.getInt("user_id"),
                    email = resultSet.getString("email"),
                    full_name = resultSet.getString("full_name"),
                    phone = resultSet.getString("phone"),
                    address = resultSet.getString("address"),
                    status = resultSet.getString("status")
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.close()
        }
    }
}
```

---

## ✅ Recommended Next Steps

1. **Use Option 1 (REST API)** - It's more secure and reliable
2. **Update API Base URL** in `ApiService.kt` with your actual domain
3. **Upload API files** to InfinityFree under `htdocs/api/`
4. **Test API endpoints** using Postman or browser
5. **Update Android app** to use the API
6. **Test the complete flow** from app to database

---

## 🧪 Testing

### Test API Endpoints

Use your browser or Postman to test:

```
https://yourdomain.infinityfreeapp.com/api/login.php
POST: email=juan.delacruz@email.com&password=password123

https://yourdomain.infinityfreeapp.com/api/get_user_loans.php
GET: ?user_id=1
```

### Test Android App

1. Run the app in Android Studio
2. Try logging in with sample user: `juan.delacruz@email.com`
3. Apply for a loan
4. Check the admin panel to see the application

---

## 🆘 Troubleshooting

### API Returns 404
- Check that files are uploaded to correct folder
- Verify URL is correct

### CORS Errors
- Add `header('Access-Control-Allow-Origin: *');` to PHP files

### Connection Timeout
- Check internet connection
- Verify InfinityFree server is running
- Check firewall settings

### Database Connection Failed
- Verify credentials are correct
- Check database exists in phpMyAdmin
- Ensure tables are created

---

## 🎉 You're All Set!

Your Android app can now communicate with the MySQL database through the PHP admin panel!
