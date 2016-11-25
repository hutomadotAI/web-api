<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 24/11/16
 * Time: 12:04
 */

namespace hutoma;


abstract class TelemetryEvent
{
    const ERROR = "ERROR";
    const INFO = "INFO";
    const WARNING = "WARNING";
    const DEBUG = "DEBUG";
}