/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.lib.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.util.List;

/**
 *
 * @author marko
 */
public interface Repository<T> {

    Future<Void> add(T entity);

    Future<T> update(T entity);

    Future<T> delete(T entity);

    Future<T> retrieveOne(Object id);

    Future<List<T>> retrieveAll();

}
