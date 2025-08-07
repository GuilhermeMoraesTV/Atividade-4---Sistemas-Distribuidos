@echo off
echo "== Executando CENARIO B (Falhas Multiplas) =="
set CLASSPATH=./bin

java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CLASSPATH%" eleicao.Simulador B

pause