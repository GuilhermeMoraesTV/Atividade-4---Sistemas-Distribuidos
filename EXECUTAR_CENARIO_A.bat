@echo off
echo "== Executando CENARIO A (Falha e Recuperacao do Coordenador) =="
set CLASSPATH=./bin

java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CLASSPATH%" eleicao.Simulador A

pause