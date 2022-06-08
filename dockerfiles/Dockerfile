FROM registry.xiaojukeji.com/didionline/oe-oe-common-image:stable

MAINTAINER daixiang <daixiang@didiglobal.com>

ENV GC_STRATEGY="-XX:+UseParallelGC -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/workspace/logs/heapdump.hprof -Xss2048k -Xmx3G -Xms3G"

ENV ES_INDEX=eng-app-store
ENV BEAT_NAME=eng-app-store
ENV LOG_PATH=/home/xiaoju/data1/oe
ENV LOG_NAME_PATTERN=default.log

RUN mkdir -p /workspace
ADD ./start.sh /workspace/start.sh
ADD ./webterminal.sh /workspace/webterminal.sh
COPY ./start.conf /etc/supervisord.d/

RUN mkdir -p /workspace/.deploy
ADD ./service.json /workspace/.deploy/service.json

ADD *.jar /workspace/

EXPOSE 22 8080
