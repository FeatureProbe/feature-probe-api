#!/bin/bash

export JAVA_HOME=/usr/local/jdk1.8.0_65
export PATH=$JAVA_HOME/bin:$PATH

mvn clean package -f ../pom.xml

ret=$?
if [ $ret -ne 0 ];then
    echo "===== maven build failed! ====="
    exit $ret
else
    echo -n "===== maven build succeeded! ====="
fi

rm -rf output
mkdir output
# 拷贝Dockerfile及其需要的文件至output目录下
cp dockerfiles/* output/
mv target/*.jar output/
