#! /bin/bash

WORK_DIR=`dirname $0`
cd $WORK_DIR

java -Xss4m -Xms512m -Xmx512m -cp snaker.jar:lib/* com.snaker.Entry
