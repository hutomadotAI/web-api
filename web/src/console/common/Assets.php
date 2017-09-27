<?php

namespace hutoma;

class Assets
{
  private $manifest;
  private $host = "/";

  public function __construct($manifest)
  {
    $this->manifest = $manifest;
  }

  public function getAsset($asset)
  {
    echo $this->host . $this->manifest[$asset];
  }

  public function getManifest()
  {
    return $manifest;
  }
}