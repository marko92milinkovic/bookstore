/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.lib.repository;

import io.vertx.core.Future;

import java.util.List;

/**
 *
 * @author marko
 */
public interface Repository<T, ID> {

    Future<Void> add(T entity);

    Future<T> update(T entity);

    Future<T> delete(T entity);

    Future<T> retrieveOne(ID id);

    Future<List<T>> retrieveAll();

}
