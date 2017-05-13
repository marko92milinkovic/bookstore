/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
'use strict';
var appControllers = angular.module('appControllers', []);

app.controller('HomeCtrl', function ($scope, $http, $rootScope, $routeParams) {
    $scope.books = [];
    $http.get('/api/bookservice/books').then(function (response) {
        console.log("RESP: " + response.data);
        $scope.books = response.data;
    });
    $http.get('/auth/customer/get').then(function (response) {
        $scope.user = response.data;
        $rootScope.user = response.data;
        console.log("in customerget. root scope " + $rootScope);
        console.log($rootScope);
    });
});

app.controller('AccountCtrl', function ($scope, $http, $rootScope) {
    $scope.user = {};
    $http.get('/auth/customer/get').then(function (response) {
        $scope.user = response.data;
        $rootScope.user = response.data;
        console.log("in customerget. root scope " + $rootScope);
        console.log($rootScope);
    });
});

app.controller('BookCtrl', function ($scope, $http, $routeParams) {
    $scope.book = {};
    $scope.inventory = true;

    $http.get('/api/bookservice/books/' + $routeParams.bookId).then(function (response) {
        console.log("sending http request");
        $scope.book = response.data;
    });
});

app.controller('CartCtrl', function ($scope, $http, $rootScope) {

    $scope.cart = {};

    $http.get('/api/cartservice/cart/').then(function (response) {
        console.log("cart is ");
        console.log(response);
        $scope.cart = response.data;
    });

});

app.controller('OrdersCtrl', function () {

});


app.controller('AddToCartCtrl', function ($scope, $http, $rootScope) {
    $scope.bookId = "";

    $scope.addToCart = function () {
        console.log("in addtocart. root scope ");
        console.log($rootScope);
        var data = {
            cartEventType: "ADD_ITEM",
            bookId: $scope.book.bookId,
            amount: $scope.amount,
            customerId: $rootScope.user !== undefined ? $rootScope.user.id : ""
        };

        $http.post("/api/cartservice/events/add", data).then(function (response) {
            console.log(response);
            //should increase number of items in cart
//            $window.location.href = '/!#/cart';
        });
    };
});

app.controller('RegisterCtrl', function ($scope, $http, $templateCache, $routeParams) {
    $scope.new_user = {};
    $http.post('/auth/customer/get', $scope.new_user).then(function (response) {
        $scope.user = response.data;
    });
});