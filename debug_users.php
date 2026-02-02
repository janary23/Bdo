<?php
// Use absolute path for config
require_once 'c:/xampp/htdocs/bdo/admin/includes/config.php';

echo "--- Recent Users Debug Info ---\n";
echo "ID | Email | Occupation | Income | Calc Limit\n";
echo "------------------------------------------------\n";

$query = "SELECT user_id, email, monthly_income, occupation FROM users ORDER BY user_id DESC LIMIT 5";
$result = mysqli_query($conn, $query);

while ($row = mysqli_fetch_assoc($result)) {
    $occupation = $row['occupation'] ?? 'NULL';
    $income = floatval($row['monthly_income'] ?? 0);
    
    // logic replication
    if (strtolower($occupation) === 'student') {
        $limit = 20000;
        $type = "Student Fixed";
    } elseif ($income > 0) {
        $limit = $income * 5;
        $type = "Income Based";
    } else {
        $limit = 50000;
        $type = "Default Min";
    }
    
    echo "{$row['user_id']} | {$row['email']} | $occupation | $income | $limit ($type)\n";
}
?>
