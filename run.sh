docker-compose up -d spark-master spark-worker-1 spark-worker-2


docker exec -i profilecreationmultiprojectsetup-kafka-1 kafka-topics  --bootstrap-server localhost:9092 --create --topic sparksink2 --replication-factor 1 --partitions 1
docker exec -i profilecreationmultiprojectsetup-kafka-1 kafka-console-producer --broker-list :9092 --topic sparksink2

runMain com.spark.app.minio.MinIOWriter localhost:9092 ./checkpoint-kafka-5