#!/bin/sh

if [ -f /etc/profile ]; then
    . /etc/profile
fi

if [ -f ~/.bash_profile ]; then
    . ~/.bash_profile
fi

echo "application is starting..."
BASE_DIR=$(cd $(dirname $0)/..; pwd)
CLASSPATH=$BASE_DIR/conf:$BASE_DIR/lib/*
echo "CLASSPATH => $CLASSPATH"

BIN_DIR=$(cd $(dirname $0); pwd)
#JAVA_OPTS="-server -Xmx2G -Xms2G -Xmn768M -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M -XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses -XX:+CMSClassUnloadingEnabled -XX:+ParallelRefProcEnabled -XX:+CMSScavengeBeforeRemark -XX:ErrorFile=$BIN_DIR/hs_err_pid%p.log -Xloggc:$BIN_DIR/gc.log -XX:HeapDumpPath=$BIN_DIR -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError"
#JAVA_OPTS="-server -Xmx4G -Xms4G -Xmn1536M -XX:MaxMetaspaceSize=512M -XX:MetaspaceSize=512M -XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses -XX:+CMSClassUnloadingEnabled -XX:+ParallelRefProcEnabled -XX:+CMSScavengeBeforeRemark -XX:ErrorFile=$BIN_DIR/hs_err_pid%p.log   -Xloggc:$BIN_DIR/gc.log -XX:HeapDumpPath=$BIN_DIR -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError"
#JAVA_OPTS="-server -Xmx8G -Xms8G -XX:MaxMetaspaceSize=512M -XX:MetaspaceSize=512M -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+ParallelRefProcEnabled -XX:ErrorFile=$BIN_DIR/hs_err_pid%p.log -Xloggc:$BIN_DIR/gc.log -XX:HeapDumpPath=$BIN_DIR -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="-server -Xmx16G -Xms16G -XX:MaxMetaspaceSize=512M -XX:MetaspaceSize=512M -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+ParallelRefProcEnabled -XX:ErrorFile=$BIN_DIR/hs_err_pid%p.log -Xloggc:$BIN_DIR/gc.log -XX:HeapDumpPath=$BIN_DIR -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError"
for arg in $*
do
    case $arg in
        -D*)
            JAVA_OPTS="$JAVA_OPTS $arg"
            ;;
        -debug)
            JAVA_OPTS="$JAVA_OPTS $DEBUG_OPTS"
            ;;
    esac
done

echo "JAVA_OPTS => $JAVA_OPTS"

PID_FILE="$BIN_DIR/pid"
if [ ! -s "$PID_FILE" ] || [ -z "$(ps -eo pid | grep -w $(cat $PID_FILE))" ]; then
    java $JAVA_OPTS -cp $CLASSPATH com.mamba.benchmark.http.Main > $BIN_DIR/std.log 2>&1 &
    echo $! > $PID_FILE
    echo "process has been started, pid=$(cat $PID_FILE)"
else
    echo "application can not start duplicate! running pid=$(cat $PID_FILE)"
fi
