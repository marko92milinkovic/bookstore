/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
'use strict';

var app = angular.module('app', [
    'ngRoute',
    'appControllers'
]);

/**
 * Config routes
 */
app.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: 'view/home.html',
            controller: 'HomeCtrl'
        }).when('/books', {
            templateUrl: 'view/books.html',
            controller: 'HomeCtrl'
        }).when('/p/private', {
            templateUrl: '../../private/private_page.html',
            controller: 'PrivateCtrl'
        }).when('/books/:bookId', {
            templateUrl: 'view/book-detail.html',
            controller: 'BookCtrl'
        }).when('/login', {
            templateUrl: 'view/login.html',
            controller: 'LoginCtrl'
        }).when('/account', {
            templateUrl: '../../private/account.html',
            controller: 'AccountCtrl'
        }).when('/orders', {
            templateUrl: '../../private/orders.html',
            controller: 'OrdersCtrl'
        }).when('/cart', {
            templateUrl: '../../private/cart.html',
            controller: 'CartCtrl'
        }).when('/about', {
            templateUrl: 'view/about.html'
        }).when('/contact', {
            templateUrl: 'view/contact.html',
            controller: 'CartCtrl'
        }).when('/register', {
            templateUrl: 'view/register.html',
            controller: 'RegisterCtrl'
        }).when('/specials', {
            templateUrl: 'view/specials.html',
        }).when('/404', {
            templateUrl: 'view/404.html'
        }).otherwise({
            redirectTo: '/404'
        })
    }]);

