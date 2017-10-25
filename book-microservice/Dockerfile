FROM java:8-jre

ENV VERTICLE_FILE target/book-microservice-1.0-SNAPSHOT-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /verticles

EXPOSE 9002

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar book-microservice-1.0-SNAPSHOT-fat.jar -Dvertx.disableDnsResolver=true -cluster -conf docker.json"]