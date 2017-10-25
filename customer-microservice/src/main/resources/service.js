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
// var Future = require('vertx-js/future');
var MongoClient = require("vertx-mongo-js/mongo_client");

// mongo configuration
var mongoconfig = {
    "connection_string": "mongodb://mongo:27017",
    "db_name": "master"
};
var collection = "customer";
//
// for (var x in vertx.getOrCreateContext()){
//     print (x);
// }
// for (var x in vertx){
//     print(vertx[x]);
//     print (x);
// }
print(vertx.getOrCreateContext().deploymentID());

var config = vertx.getOrCreateContext().config();

print(vertx.isEventLoopContext);

print("Config in js: " +JSON.stringify(config));

var mongoClient = MongoClient.createShared(vertx, config);
var getCustomerByUsername = function (username, handler) {
}
getCustomerByUsername()

CustomerService.registerService(
    vertx,
    // this is the JS facade
    new CustomerService(
        // this is the Java Interface implementation
        new JCustomerService({
            getCustomerByUsername: function (username, handler) {
                mongoClient.find(collection, {username: username}, function (res, res_err) {
                    if (res_err === null) {
                        handler(Future.succeededFuture(new Customer(res)));
                    } else {
                        handler(Future.failedFuture(res_err));
                    }
                });
            },

            createNewCustomer: function (customer, handler) {
                mongoClient.save(customer, function (res, res_err) {
                    if (res_err === null) {
                        handler(Future.succeededFuture(new Customer(res)));
                    } else {
                        handler(Future.failedFuture(res_err));
                    }
                })
            }
        })
    )
);
