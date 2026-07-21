$ErrorActionPreference = 'Stop'

$erlangHome = 'C:\Program Files\Erlang OTP'
$rabbitHome = Join-Path $env:USERPROFILE 'tools\rabbitmq_server-4.3.3'
$rabbitBase = Join-Path $env:USERPROFILE '.rabbitmq'

$env:ERLANG_HOME = $erlangHome
$env:RABBITMQ_BASE = $rabbitBase
$env:Path = "$erlangHome\bin;$rabbitHome\sbin;$env:Path"

if (-not (Test-Path (Join-Path $rabbitHome 'sbin\rabbitmq-diagnostics.bat'))) {
  throw "RabbitMQ was not found at $rabbitHome."
}

& (Join-Path $rabbitHome 'sbin\rabbitmq-diagnostics.bat') status
