#!/bin/sh

# ======== JVM settings =======
javaopts=" -Xms32m"
javaopts="$javaopts -Xmx64m"
javaopts="$javaopts -XX:SurvivorRatio=8"
javaopts="$javaopts -Xincgc"
javaopts="$javaopts -XX:+AggressiveOpts"

# не изменять
java_settings=" -Dfile.encoding=UTF-8"
java_settings="$java_settings -Djava.net.preferIPv4Stack=true"

while :; do
	./backup_db.sh
	mv log/java-0.log "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	mv log/error-0.log "log/`date +%Y-%m-%d_%H-%M-%S`_error.log"
	mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -server $java_settings $javaopts -cp config:./lib/* jts.loginserver.LoginServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 10;
done
