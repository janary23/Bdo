package com.example.bdo

/**
 * Database Configuration for BDO Loan App
 * Connects to InfinityFree MySQL Database
 */
object DatabaseConfig {
    
    // InfinityFree MySQL Credentials
    const val DB_HOST = "sql211.infinityfree.com"
    const val DB_PORT = "3306"
    const val DB_NAME = "if0_41042307_bdo"
    const val DB_USER = "if0_41042307"
    const val DB_PASSWORD = "6FASKXmhvN9JxTc"
    
    // JDBC Connection String
    const val JDBC_URL = "jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"
    
    // API Base URL (if using REST API approach)
    const val API_BASE_URL = "https://yourdomain.infinityfreeapp.com/api/"
    
    // Connection timeout settings
    const val CONNECTION_TIMEOUT = 10000 // 10 seconds
    const val READ_TIMEOUT = 15000 // 15 seconds
}
