docker run --name nats -d \
-v /opt/nats/nats.conf:/etc/nats.conf \
--restart=always \
-p 4222:4222 \
-v /opt/nats/data:/data \
hub.sudytech.cn/library/nats:2.10.23-alpine -c /etc/nats.conf
