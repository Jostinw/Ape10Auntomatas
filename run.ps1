Write-Host "Iniciando el Servidor Web del Rover Espacial..." -ForegroundColor Green
Write-Host "Servidor corriendo en http://localhost:8080" -ForegroundColor Green
java -cp "lib/java-cup-runtime-11b-20160615.jar;build" rover.ServidorWeb
