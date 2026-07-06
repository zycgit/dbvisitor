#!/bin/bash
# Start SQL Server
/opt/mssql/bin/sqlservr &
PID=$!

# Wait for SQL Server to be ready
for i in $(seq 1 30); do
    /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Share123456!' -Q "SELECT 1" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "[MSSQL] Ready. Creating devtester database if not exists..."
        /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Share123456!' -Q "IF DB_ID('devtester') IS NULL CREATE DATABASE devtester"
        break
    fi
    echo "[MSSQL] Waiting for SQL Server... ($i/30)"
    sleep 2
done

wait $PID
