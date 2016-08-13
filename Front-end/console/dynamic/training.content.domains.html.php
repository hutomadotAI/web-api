<?php
$dev_token = \hutoma\console::getDevToken();
$usr_domains = \hutoma\console::getDomains_and_UserActiveDomains($dev_token,$_SESSION['aiid']);
unset($dev_token);
?>

<div class="box box-solid box-clean flat no-shadow" >
      <a data-toggle="collapse" data-parent="#accordion" href="#collapseDomain">
          <div class="box-header with-border">
          <i class="fa fa fa-th text-warning"></i>
          <h3 class="box-title">Actived Domains</h3>
          <div class="box-tools pull-right">
            <button class="btn btn-box-tool" ><i class="fa fa-minus"></i></button>
          </div>
      </div>
      </a>
      
      <div id="collapseDomain" class="panel-collapse collapse">
      <div class="box-body">

          <?php
                $list ="<div class='row'>";
                for($i=0; $i<sizeof($usr_domains); $i++) {
                    $list = $list."<div class='col-md-3 col-sm-4 col-xs-4'>";
                    if ( $usr_domains[$i]['active'] ) {
                        $str = $usr_domains[$i]['name'];
                        if (strlen($str) > 14)
                            $str = substr($str, 0, 13).".";
                        $list .= " <a class='btn btn-app'><i class='".$usr_domains[$i]['icon']."'></i>".$str."</a>";
                    }
                    $list  = $list ."</div>";
                }
          $list  = $list ."</div>";
          echo $list;
          unset($usr_domains);
          ?>
          
      </div>
      </div>
</div>



