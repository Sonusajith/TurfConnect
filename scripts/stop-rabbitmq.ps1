$ErrorActionPreference = 'Stop'

$erlangHome = 'C:\Program Files\Erlang OTP'
$rabbitHome = Join-Path $env:USERPROFILE 'tools\rabbitmq_server-4.3.3'
$rabbitBase = Join-Path $env:USERPROFILE '.rabbitmq'

$env:ERLANG_HOME = $erlangHome
$env:RABBITMQ_BASE = $rabbitBase
$env:Path = "$erlangHome\bin;$rabbitHome\sbin;$env:Path"

if (Test-Path (Join-Path $rabbitHome 'sbin\rabbitmqctl.bat')) {
  & (Join-Path $rabbitHome 'sbin\rabbitmqctl.bat') stop
} else {
  $processIds = Get-NetTCPConnection -LocalPort 5672 -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique
  foreach ($procId in $processIds) {
    Stop-Process -Id $procId -Force
  }
}
