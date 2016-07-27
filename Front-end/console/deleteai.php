<?php
  require "../pages/config.php";

  if ( !\hutoma\console::isSessionActive()) {
    header('Location: ./error.php?err=1');
    exit;
  }

  if (isset($_SESSION["aiid"])) {

    $aiid = $_SESSION["aiid"];
    $dev_token = \hutoma\console::getDevToken();
    $response = \hutoma\console::deleteAI($dev_token, $aiid);
    unset($aiid);
    unset($dev_token);

    if ($response['status']['code'] === 200) {

      unset($_SESSION['aiid']);
      unset($_SESSION['ai_name']);
      unset($_SESSION['ai_type']);
      unset($_SESSION['ai_language']);
      unset($_SESSION['ai_timezone']);
      unset($_SESSION['ai_confidence']);
      unset($_SESSION['ai_description']);
      unset($_SESSION['ai_created_on']);
      unset($_SESSION['ai_training_status']);
      unset($_SESSION['ai_status']);
      unset($_SESSION['ai_deep_learning_error']);
      unset($_SESSION['userActivedDomains']);
      unset($_SESSION['current_ai_name']);

    }
    else{
      unset($response);
      header('Location: ./error.php?err=9');
      exit;
    }
    unset($response);
    header('Location: ./home.php');
  }
?>

