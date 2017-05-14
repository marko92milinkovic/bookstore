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
});
app.controller('AccountCtrl', function ($scope, $http, $rootScope, $location) {
    $scope.user = {};
    $http.get('/auth/customer/get').then(function (response) {
        $scope.user = response.data;
        $rootScope.user = response.data;
        console.log("in customerget. root scope " + $rootScope);
        console.log($rootScope);
    }, function (error) {
        $rootScope.user = {};
        console.log("error: " + error);
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
app.controller('CartCtrl', function ($scope, $http, $rootScope, $location) {

    $scope.cart = {};
    $scope.shipping = 25;
    $scope.total = 0;
    var getCart = function () {
        $http.get('/api/cartservice/cart').then(function (response) {
            console.log("cart is ");
            console.log(response);
            $scope.cart = response.data;
            $scope.total = 0;
            console.log($scope.cart.bookItems);
            for (var i = 0; i < $scope.cart.bookItems.length; i++) {
                var item = $scope.cart.bookItems[i];
                $scope.total += (item.price * item.amount);
            }
        }, function (error) {

        });
    };
    getCart();
    $scope.removeItem = function (amount, bookId) {
        console.log("removing item: " + bookId + " amount: " + amount);
        $http.post("/api/cartservice/events/add", {
            cartEventType: "REMOVE_ITEM",
            bookId: bookId,
            amount: parseInt(amount, 10),
            customerId: $rootScope.user !== undefined ? $rootScope.user.id : ""
        }).then(function (response) {
            console.log(response);
            //should increase number of items in cart
//            $window.location.href = '/!#/cart';
            getCart();
        }, function (error) {});
    };

    $scope.checkout = function () {
        console.log("sending checkout request");
        $http.get("/api/cartservice/checkout").then(function (response) {
            console.log(response);
            $location.path("/orders");
            getCart();
        }, function (error) {
            console.log("checkour error: ");
            console.log(error);
        });
    };


});
app.controller('OrdersCtrl', function () {

});
app.controller('AddToCartCtrl', function ($scope, $http, $rootScope) {
    $scope.bookId = "";
    $scope.addToCart = function () {
        console.log("in addtocart. root scope ");
        console.log($rootScope);
        $http.post("/api/cartservice/events/add", {
            cartEventType: "ADD_ITEM",
            bookId: $scope.book.bookId,
            amount: $scope.amount,
            customerId: $rootScope.user !== undefined ? $rootScope.user.id : ""
        }).then(function (response) {
            console.log(response);
            //should increase number of items in cart
//            $window.location.href = '/!#/cart';
        }, function (error) {});
    };
});
app.controller('RegisterCtrl', function ($scope, $http, $templateCache, $routeParams) {
    $scope.new_user = {};
    $http.post('/auth/customer/get', $scope.new_user).then(function (response) {
        $scope.user = response.data;
    });
});
app.controller('HeaderCtrl', function ($scope, $http, $location, $rootScope) {
    $rootScope.user = {};
    $scope.user = {};

    $scope.logout = function () {
        $http.get('/logout').then(function (response) {
            $scope.user = {};
            $rootScope.user = {};
            $location.path("/");
        }, function (error) {
            console.log("error: " + error);
        });
    };

    $http.get('/auth/customer/get').then(function (response) {
        $scope.user = response.data;
        $rootScope.user = response.data;
        console.log("in customerget. root scope " + $rootScope);
        console.log($rootScope);
    }, function (error) {
        $rootScope.user = {};
        console.log("error: " + error);
    });
});

  