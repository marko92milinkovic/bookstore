/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
'use strict';
var appControllers = angular.module('appControllers', []);

app.controller('HomeCtrl', function ($scope, $http, $templateCache, $routeParams) {
    $scope.books = [];
    $http.get('/api/bookservice/books').then(function (response) {
        console.log("RESP: " + response.data);
        $scope.books = response.data;
    });
});

app.controller('AccountCtrl', function ($scope, $http, $templateCache, $routeParams) {
    $scope.user = {};
    $http.get('/auth/customer/get').then(function (response) {
        $scope.user = response.data;
    });
});

app.controller('BookCtrl', function ($scope, $http, $templateCache, $routeParams) {
    $scope.book = {};
    $scope.inventory = true;

    $http.get('/api/bookservice/books/' + $routeParams.bookId).then(function (response) {
        console.log("sending http request");
        $scope.book = response.data;
    });
});

app.controller('CartCtrl', function () {
    
});

app.controller('OrdersCtrl', function () {

});


app.controller('AddToCartCtrl', function ($scope, $http, $rootScope) {
    $scope.bookId = "";

    $scope.addToCart = function () {
        var data = {
            cartEventType: "ADD_ITEM",
            bookId: $scope.book.bookId
        };

        $http.post("/api/cart/events", data).then(function (response) {
            //should increase number of items in cart
        });
    };
});