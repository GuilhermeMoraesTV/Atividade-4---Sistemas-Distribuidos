@echo off
rem Garante que o script seja executado no diretorio do projeto
cd /d "%~dp0"

echo --- Limpando compilacoes antigas...
rem --- CORREÇÃO AQUI: "rmdir" é o comando correto ---
if exist bin rmdir /s /q bin
mkdir bin
echo.
echo --- Compilando todo o projeto...

rem Cria uma lista com o caminho completo de todos os arquivos .java
dir /s /b src\*.java > sources.txt

rem Adicionado "2>&1" para garantir que as mensagens de erro do compilador sejam exibidas
javac -d bin -encoding UTF-8 @sources.txt 2>&1

rem Deleta o arquivo temporario
del sources.txt

echo.
if exist "bin\eleicao\SimuladorBully.class" (
    echo Compilacao finalizada com SUCESSO!
) else (
    echo ***** FALHA NA COMPILACAO! *****
    echo Verifique as mensagens de erro acima.
)

pause