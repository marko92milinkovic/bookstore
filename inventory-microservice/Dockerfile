FROM java:8-jre

ENV VERTICLE_FILE target/inventory-microservice-1.0-SNAPSHOT-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 9003

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/src/conf/config.json
COPY src/main/resources/ $VERTICLE_HOME/src/main/resources/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar inventory-microservice-1.0-SNAPSHOT-fat.jar -Dvertx.disableDnsResolver=true -cluster -conf docker.json"]