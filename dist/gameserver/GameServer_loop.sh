#!/bin/sh

# ======== JVM settings =======
# Set heap min/max to same size for consistent results
# одинаковый размер памяти для Xms и Xmx, JVM пытается удержать размер heap'а минимальным, и если его нужно меньше, чем в Xmx - гоняет GC понапрасну
javaopts=" -Xms4096m"
javaopts="$javaopts -Xmx4096m"

# Non Heap memory
javaopts="$javaopts -XX:PermSize=512m"
# Maximum size of the permanent generation.
javaopts="$javaopts -XX:MaxPermSize=640m"

# Garbage collection/Performance Options
javaopts="$javaopts -XX:+UseConcMarkSweepGC"
javaopts="$javaopts -XX:+UseParNewGC"
javaopts="$javaopts -XX:+CMSIncrementalMode"
javaopts="$javaopts -XX:MaxGCPauseMillis=500"
javaopts="$javaopts -XX:+DoEscapeAnalysis"
javaopts="$javaopts -XX:+UseBiasedLocking"
javaopts="$javaopts -XX:+EliminateLocks"
# javaopts="$javaopts -XX:CMSIncrementalSafetyFactor=50"
# Number of garbage collector threads for the parallel young generation collections and for the parallel parts of the old generation collections
javaopts="$javaopts -XX:ParallelGCThreads=10"
javaopts="$javaopts -XX:ParallelCMSThreads=5"

javaopts="$javaopts -XX:+AggressiveOpts"
# Default size of new generation
# javaopts="$javaopts -XX:NewSize=512m"
# javaopts="$javaopts -XX:MaxNewSize=1024m"
# instructs the VM to set a 2:1 ratio between young and tenured generations (Ratio of new/old generation sizes)
# javaopts="$javaopts -XX:NewRatio=2"
# Sets survivor space ratio to 1:8, resulting in larger survivor spaces (the smaller the ratio, the larger the space). Larger survivor spaces allow short lived objects a longer time period to die in the young generation
# javaopts="$javaopts -XX:SurvivorRatio=8"
# javaopts="$javaopts -XX:TargetSurvivorRatio=80"
# javaopts="$javaopts -XX:MaxTenuringThreshold=10"

javaopts="$javaopts -XX:+UseCMSInitiatingOccupancyOnly"
javaopts="$javaopts -XX:CMSInitiatingOccupancyFraction=80"

# javaopts="$javaopts -XX:+CMSParallelRemarkEnabled"
javaopts="$javaopts -XX:+CMSClassUnloadingEnabled"

# The important setting in 64-bits with the Sun JVM is -XX:+UseCompressedOops as it saves memory and improves performance
javaopts="$javaopts -XX:+UseCompressedOops"
javaopts="$javaopts -XX:+UseFastAccessorMethods"

# Logging
# javaopts="$javaopts -XX:+PrintGCDetails"
# javaopts="$javaopts -XX:+PrintGCDateStamps"
# javaopts="$javaopts -XX:+PrintGCApplicationStoppedTime"
# javaopts="$javaopts -XX:+PrintGCTimeStamps"
# javaopts="$javaopts -XX:+PrintGC"
# javaopts="$javaopts -Xloggc:./log/game/garbage_collector.log"

# не изменять
java_settings=" -Dfile.encoding=UTF-8"
java_settings="$java_settings -Djava.net.preferIPv4Stack=true"

# ==========================================

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

while :;
do
	./backup_db.sh
	mv log/java-0.log "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	mv log/error-0.log "log/`date +%Y-%m-%d_%H-%M-%S`_error.log"
	mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -server $java_settings $javaopts -cp config:./lib/* jts.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 10;
done
