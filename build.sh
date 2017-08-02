
#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build -t "bookstore/api-gateway" $DIR/api-gateway
docker build -t "bookstore/inventory-microservice" $DIR/inventory-microservice
docker build -t "bookstore/order-microservice" $DIR/order-microservice
docker build -t "bookstore/book-microservice" $DIR/book-microservice
docker build -t "bookstore/cart-microservice" $DIR/cart-service
docker build -t "bookstore/customer-microservice" $DIR/customer-microservice