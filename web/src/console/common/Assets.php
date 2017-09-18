<?php

namespace hutoma;

class Assets
{
  private $manifest;
  private $host = "/";

  public function __construct()
  {
    $this->manifest = yaml_parse_file(__DIR__ . "/../dist/manifest.yml");   
  }

  public function getAsset($asset)
  {
    echo $this->host . $this->manifest[$asset];
  }
}