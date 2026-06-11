# Crear directorio de compilación si no existe
if (-not (Test-Path "build")) {
    New-Item -ItemType Directory -Path "build" | Out-Null
}

Write-Host "Generando Analizador Léxico con JFlex..." -ForegroundColor Cyan
java -cp "lib/jflex-1.9.1.jar;lib/java-cup-runtime-11b-20160615.jar" jflex.Main -d src/rover src/rover/lexer.flex

Write-Host "Generando Analizador Sintáctico con CUP..." -ForegroundColor Cyan
java -cp "lib/java-cup-11b-20160615.jar" java_cup.Main -parser parser -symbols sym -destdir src/rover src/rover/parser.cup

Write-Host "Compilando clases Java con javac..." -ForegroundColor Cyan
javac -cp "lib/java-cup-runtime-11b-20160615.jar;src" -d build src/rover/*.java

Write-Host "¡Proceso de construcción finalizado con éxito!" -ForegroundColor Green
