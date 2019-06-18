#!/bin/sh

echo "application is shutting down..."
PID_FILE=$(cd $(dirname $0); echo "$(pwd)/pid")
pid=$(cat "$PID_FILE")
echo "killing pid $pid"
kill $pid && sleep 3
if [ -n "$(ps -eo pid | grep $pid)" ]; then
    kill -9 $pid
fi
cat /dev/null > $PID_FILE
echo "process has been shutdown, pid=$pid"
