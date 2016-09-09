<?php
  require "../pages/config.php";

  if ( !\hutoma\console::isSessionActive()) {
    header('Location: ./error.php?err=1');
    exit;
  }

  if (isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'])) {

    $response = \hutoma\console::deleteAI(\hutoma\console::getDevToken(), $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);

    if ($response['status']['code'] === 200) {

      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['timezone']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['personality']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['sex']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['userActivedDomains']);

      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['created_on']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['is_private']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['deep_learning_error']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_debug_info']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_status']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['status'] );
      //unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_file']);

      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['contract']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['payment_type']);
      unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['price']);

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

