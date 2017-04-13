/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var app = angular.module('app', []);

app.controller('BookController', function ($http) {
    var self = this;
    $http.get('/api/bookservice/books/1').then(function (response) {
        console.log("sending http request");
        self.book = response.data;
    });
});