/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.lib.repository;

import java.util.Optional;
import rx.Observable;
import rx.Single;

/**
 *
 * @author markom
 */
public interface RxRepository<T, ID> {
    Single<Optional<T>> retrieveOne(ID id);
    Single<T> updateOne(ID id, T entity);
    Single<Void> addOne(T entity);
    Observable<T> retrieveAll();
    Single<T> deleteOne (ID id);
}
