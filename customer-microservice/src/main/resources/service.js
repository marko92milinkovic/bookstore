/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



var Future = Java.type('io.vertx.core.Future');
var CustomerService = require('customer-service-js/customer_service');
// force nashorn to convert to this type later on
var JCustomerService = Java.type('rs.bookstore.customer.service.CustomerService');
var Customer = Java.type('rs.bookstore.customer.service.Customer');

var utils = require('vertx-js/util/utils');
var MongoClient = require("vertx-mongo-js/mongo_client");

// mongo configuration
var mongoconfig = {
    "connection_string": "mongodb://mongo:27017",
    "db_name": "master"
};
var collection = "customer";
//


var mongoClient = MongoClient.createShared(vertx, mongoconfig);

CustomerService.registerService(
        vertx,
        // this is the JS facade
        new CustomerService(
                // this is the Java Interface implementation
                new JCustomerService({
                    getCustomerByUsername: function (username, handler) {

                        mongoClient.find(collection, {username: username}, function (res, res_err) {
                            if (res_err === null) {
                                Array.prototype.forEach.call(res, function (json) {
                                    console.log(JSON.stringify(json));
                                });
                                console.log(utils.convParamJsonObject(res[0]));
                                handler(Future.succeededFuture(new Customer(utils.convParamJsonObject(res[0]))));
                            } else {
                                console.log("MA dajdeeadiafsdafijadwf[dsof");
                                res_err.printStackTrace();
                                handler(Future.failedFuture(res_err));
                            }
                        });
                    }
                })
                )
        );
