$ErrorActionPreference = 'Stop'

$erlangHome = 'C:\Program Files\Erlang OTP'
$rabbitHome = Join-Path $env:USERPROFILE 'tools\rabbitmq_server-4.3.3'
$rabbitBase = Join-Path $env:USERPROFILE '.rabbitmq'

if (-not (Test-Path (Join-Path $erlangHome 'bin\erl.exe'))) {
  throw "Erlang was not found at $erlangHome. Install Erlang OTP 27 before starting RabbitMQ."
}

if (-not (Test-Path (Join-Path $rabbitHome 'sbin\rabbitmq-server.bat'))) {
  throw "RabbitMQ was not found at $rabbitHome. Install RabbitMQ 4.3.3 before starting it."
}

$env:ERLANG_HOME = $erlangHome
$env:RABBITMQ_BASE = $rabbitBase
$env:Path = "$erlangHome\bin;$rabbitHome\sbin;$env:Path"

New-Item -ItemType Directory -Force -Path $rabbitBase | Out-Null

$listener = Get-NetTCPConnection -LocalPort 5672 -State Listen -ErrorAction SilentlyContinue
if ($listener) {
  Write-Host "RabbitMQ is already listening on port 5672."
  exit 0
}

& (Join-Path $rabbitHome 'sbin\rabbitmq-server.bat') -detached
Start-Sleep -Seconds 8

& (Join-Path $rabbitHome 'sbin\rabbitmq-plugins.bat') enable rabbitmq_management | Out-Host

$ports = Get-NetTCPConnection -LocalPort 5672,15672 -State Listen -ErrorAction SilentlyContinue |
  Select-Object LocalPort, OwningProcess |
  Sort-Object LocalPort

if (-not $ports) {
  throw "RabbitMQ start command ran, but ports 5672/15672 are not listening yet. Check $rabbitBase\log."
}

$ports | Format-Table -AutoSize
Write-Host "RabbitMQ AMQP: localhost:5672"
Write-Host "RabbitMQ Management UI: http://localhost:15672"
Write-Host "Default login: guest / guest"
