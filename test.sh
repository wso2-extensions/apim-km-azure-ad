./mvnw clean install
cp components/azure.key.manager/target/manager-1.0-SNAPSHOT.jar dockerfiles/apim/
docker-compose up --build