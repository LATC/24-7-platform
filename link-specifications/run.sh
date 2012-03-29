#!/bin/bash
TASKS="eventseer-dblp_l3s-event eventseer-dblp_l3s-publications eventseer-dblp_rkb-person"
SILK="/home/cgueret/tmp/silk_2.4.1/silk.jar" 

for task in $TASKS
do
	echo $task
	cd $task
	rm -f log.*
	java -DconfigFile=spec.xml -jar ${SILK} >log.txt 2>&1
	cd -
done
