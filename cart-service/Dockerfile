FROM java:8-jre

ENV VERTICLE_FILE target/cart-service-1.0-SNAPSHOT-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 9051

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/src/conf/config.json

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar cart-service-1.0-SNAPSHOT-fat.jar -cluster -conf docker.json"]