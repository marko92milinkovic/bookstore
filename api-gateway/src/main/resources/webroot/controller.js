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
    $scope.user= {};
        $http.get('/auth/customer/get').then(function (response) {
        $scope.user = response.data;
    });
});

app.controller('LoginCtrl', function ($scope, $http, $templateCache, $routeParams) {
    console.log("LogIn CTRL was looked for");
//    $scope.credentials = {};
//    $scope.login = function () {
//        console.log("subited");
//        var username = $scope.credentials.username;
//        var password = $scope.credentials.password;
//
//        var auth = {username: username, password: password};
//        console.log("Saljem podatke: " + auth);
//        $http.post('/loginhandler', auth).then(function (response) {
//            console.log("login RESP: " + response.data);
//        });
//    };
});



app.controller('BookCtrl', function ($scope, $http, $templateCache, $routeParams) {
    $scope.book = {};
    $http.get('/api/bookservice/books/' + $routeParams.bookId).then(function (response) {
        console.log("sending http request");
        $scope.book = response.data;
    });
});
