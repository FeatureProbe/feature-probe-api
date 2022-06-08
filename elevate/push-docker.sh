./mvnw package

docker build -t hub.xiaojukeji.com/dengfeige/feature-probe/api .
docker push hub.xiaojukeji.com/dengfeige/feature-probe/api

echo "Push docker image successful."
