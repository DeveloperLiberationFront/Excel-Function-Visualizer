<?php
  $username = "root";
  $password = getenv("MYSQL_PASSWORD");
  $host = "localhost:3306";
  $database = "spreadsheet_funcs";

  $con = new mysqli($host, $username, $password, $database);
  if ($con->connect_errno) {
    printf("Database connection failed: %s\n", $con->connect_errno);
    die;
  }

  $query = "SELECT * FROM formulas_unique WHERE id = " . $_GET["id"] . ";";
  $result = $con->query($query) or die(mysqli_error($con));

  $data = mysqli_fetch_assoc($result);
  echo json_encode($data);
  mysqli_close($con);
?>
