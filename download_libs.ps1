$libDir = "lib"
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir | Out-Null
}

Write-Host "Descargando JFlex 1.9.1..."
curl.exe -L -o "lib/jflex-1.9.1.jar" "https://repo1.maven.org/maven2/de/jflex/jflex/1.9.1/jflex-1.9.1.jar"

Write-Host "Descargando Java CUP 11b..."
curl.exe -L -o "lib/java-cup-11b-20160615.jar" "https://repo1.maven.org/maven2/com/github/vbmacher/java-cup/11b-20160615/java-cup-11b-20160615.jar"

Write-Host "Descargando Java CUP Runtime 11b..."
curl.exe -L -o "lib/java-cup-runtime-11b-20160615.jar" "https://repo1.maven.org/maven2/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar"

Write-Host "¡Descarga de librerías completada con éxito!"
