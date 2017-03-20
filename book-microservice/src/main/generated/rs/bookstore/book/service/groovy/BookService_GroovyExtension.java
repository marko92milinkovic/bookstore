package rs.bookstore.book.service.groovy;
public class BookService_GroovyExtension {
  public static rs.bookstore.book.service.BookService addBook(rs.bookstore.book.service.BookService j_receiver, java.util.Map<String, Object> book, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> resultHandler) {
    rs.bookstore.book.service.groovy.internal.ConversionHelper.wrap(j_receiver.addBook(book != null ? new rs.bookstore.book.domain.Book(rs.bookstore.book.service.groovy.internal.ConversionHelper.toJsonObject(book)) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>>() {
      public void handle(io.vertx.core.AsyncResult<java.lang.Void> ar) {
        resultHandler.handle(ar.map(event -> rs.bookstore.book.service.groovy.internal.ConversionHelper.wrap(event)));
      }
    } : null));
    return j_receiver;
  }
  public static rs.bookstore.book.service.BookService getBook(rs.bookstore.book.service.BookService j_receiver, long bookID, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    rs.bookstore.book.service.groovy.internal.ConversionHelper.wrap(j_receiver.getBook(bookID,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<rs.bookstore.book.domain.Book>>() {
      public void handle(io.vertx.core.AsyncResult<rs.bookstore.book.domain.Book> ar) {
        resultHandler.handle(ar.map(event -> rs.bookstore.book.service.groovy.internal.ConversionHelper.applyIfNotNull(event, a -> rs.bookstore.book.service.groovy.internal.ConversionHelper.fromJsonObject(a.toJson()))));
      }
    } : null));
    return j_receiver;
  }
  public static rs.bookstore.book.service.BookService getAllBooks(rs.bookstore.book.service.BookService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<java.util.Map<String, Object>>>> resultHandler) {
    rs.bookstore.book.service.groovy.internal.ConversionHelper.wrap(j_receiver.getAllBooks(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<rs.bookstore.book.domain.Book>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.List<rs.bookstore.book.domain.Book>> ar) {
        resultHandler.handle(ar.map(event -> rs.bookstore.book.service.groovy.internal.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> rs.bookstore.book.service.groovy.internal.ConversionHelper.applyIfNotNull(elt, a -> rs.bookstore.book.service.groovy.internal.ConversionHelper.fromJsonObject(a.toJson()))).collect(java.util.stream.Collectors.toList()))));
      }
    } : null));
    return j_receiver;
  }
  public static rs.bookstore.book.service.BookService deleteBook(rs.bookstore.book.service.BookService j_receiver, long bookID, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    rs.bookstore.book.service.groovy.internal.ConversionHelper.wrap(j_receiver.deleteBook(bookID,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<rs.bookstore.book.domain.Book>>() {
      public void handle(io.vertx.core.AsyncResult<rs.bookstore.book.domain.Book> ar) {
        resultHandler.handle(ar.map(event -> rs.bookstore.book.service.groovy.internal.ConversionHelper.applyIfNotNull(event, a -> rs.bookstore.book.service.groovy.internal.ConversionHelper.fromJsonObject(a.toJson()))));
      }
    } : null));
    return j_receiver;
  }
}
