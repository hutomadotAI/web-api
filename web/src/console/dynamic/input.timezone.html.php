<?php
$zones = timezone_identifiers_list();

$html = '';
$html .= '<div class="form-group">';
$html .= '<label for="ai_timezone">Time zone</label>';
$html .= '<select class="form-control select2" name="ai_timezone" id="ai_timezone" style="width: 100%;">';

foreach ($zones as $zone) {
  $zone = explode('/', $zone); // 0 => Continent, 1 => City
  // Only use "friendly" continent names
  if ($zone[0] == 'Africa' ||
      $zone[0] == 'America'||
      $zone[0] == 'Antarctica' ||
      $zone[0] == 'Arctic' ||
      $zone[0] == 'Asia' ||
      $zone[0] == 'Atlantic' ||
      $zone[0] == 'Australia' ||
      $zone[0] == 'Europe' ||
      $zone[0] == 'Indian' ||
      $zone[0] == 'Pacific')

      if (isset($zone[1]) != '') {
          $timezone = $zone[0] . '/' . $zone[1];

          if (isset($zone[2]) != '')
              $timezone .= '/' . $zone[2];

          if ($timezone == 'Europe/London')
            $html .= '<option selected="selected">'. $timezone .'</option>';
          else
            $html .= '<option>'. $timezone .'</option>';
    }
}

$html .= '</select></div>';
echo $html;

