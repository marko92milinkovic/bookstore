FROM java:8-jre

ENV VERTICLE_FILE target/api-gateway-1.0-SNAPSHOT-fat.jar 

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 8080

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME


WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar api-gateway-1.0-SNAPSHOT-fat.jar -Dvertx.disableDnsResolver=true -cluster -ha -conf docker.json"]
