# PowerShell Maven Wrapper helper script
$MAVEN_VERSION = "3.9.6"
$M2_HOME = "$PSScriptRoot\.maven"
$M2_ZIP = "$PSScriptRoot\.maven.zip"

if (!(Test-Path $M2_HOME)) {
    Write-Host "Maven not found. Downloading Apache Maven $MAVEN_VERSION..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    try {
        Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip" -OutFile $M2_ZIP
        Write-Host "Extracting Maven..."
        Expand-Archive -Path $M2_ZIP -DestinationPath $PSScriptRoot
        Rename-Item -Path "$PSScriptRoot\apache-maven-$MAVEN_VERSION" -NewName ".maven"
        Remove-Item -Path $M2_ZIP
        Write-Host "Maven downloaded and configured successfully."
    } catch {
        Write-Error "Failed to download Maven: $_"
        exit 1
    }
}

$env:M2_HOME = $M2_HOME
& "$M2_HOME\bin\mvn.cmd" $args
