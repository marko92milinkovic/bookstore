/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module book-service-js/book_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JBookService = Java.type('rs.bookstore.book.service.BookService');
var Book = Java.type('rs.bookstore.book.domain.Book');

/**
 
 @class
*/
var BookService = function(j_val) {

  var j_bookService = j_val;
  var that = this;

  /**

   @public
   @param book {Object} 
   @param resultHandler {function} 
   @return {BookService}
   */
  this.addBook = function(book, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_bookService["addBook(rs.bookstore.book.domain.Book,io.vertx.core.Handler)"](book != null ? new Book(new JsonObject(Java.asJSONCompatible(book))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param bookID {number} 
   @param resultHandler {function} 
   @return {BookService}
   */
  this.getBook = function(bookID, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_bookService["getBook(long,io.vertx.core.Handler)"](bookID, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   @return {BookService}
   */
  this.getAllBooks = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_bookService["getAllBooks(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param bookID {number} 
   @param resultHandler {function} 
   @return {BookService}
   */
  this.updateBook = function(bookID, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_bookService["updateBook(long,io.vertx.core.Handler)"](bookID, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param bookID {number} 
   @param resultHandler {function} 
   @return {BookService}
   */
  this.deleteBook = function(bookID, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_bookService["deleteBook(long,io.vertx.core.Handler)"](bookID, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_bookService;
};

BookService._jclass = utils.getJavaClass("rs.bookstore.book.service.BookService");
BookService._jtype = {
  accept: function(obj) {
    return BookService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(BookService.prototype, {});
    BookService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
BookService._create = function(jdel) {
  var obj = Object.create(BookService.prototype, {});
  BookService.apply(obj, arguments);
  return obj;
}
module.exports = BookService;