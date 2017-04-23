/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews.repository;

import rs.bookstore.lib.repository.RxRepository;
import rs.bookstore.reviews.Review;
import rx.Observable;

/**
 *
 * @author marko
 */
public interface ReviewRxRepository extends RxRepository<Review, Long>{
    
     Observable<Review> retrieveByBookId(Long bookId);
    
}
