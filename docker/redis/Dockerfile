FROM ubuntu:23.10
RUN apt-get update; \
    apt-get install -y wget; \
    apt-get install -y make; \
    apt-get install -y gcc; \
    cd ~; \
    mkdir software; \
    cd software; \
    wget https://download.redis.io/releases/redis-6.2.14.tar.gz; \
    tar -zxf redis-6.2.14.tar.gz; \
    mv redis-6.2.14 redis; \
    cd redis; \
    make; \
    make install
