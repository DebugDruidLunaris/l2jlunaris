#!/bin/bash

while :;
do
	java -server -Dfile.encoding=UTF-8 -Xms32m -Xmx64m -cp config:./lib/* jts.loginserver.GameServerRegister
	[ $? -ne 2 ] && break
	sleep 10;
done
