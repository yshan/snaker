#! /bin/bash

WORK_DIR=`dirname $0`
cd $WORK_DIR

java -Dfile.encoding=UTF8 -Xss4m -Xms512m -Xmx512m -cp snaker.jar:lib/* com.snaker.Entry
