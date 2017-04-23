/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews.repository;

import io.vertx.core.Future;
import java.util.List;
import rs.bookstore.lib.repository.Repository;
import rs.bookstore.reviews.Review;

/**
 *
 * @author marko
 */
public interface ReviewRepository extends Repository<Review, Long> {
    Future<List<Review>> retrieveByBookId(Long bookId);

}
