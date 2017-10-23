package rs.bookstore.apigateway;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.function.Supplier;

public class BooksHystrixCommand<T> extends HystrixObservableCommand<T> {

    Supplier<Observable<T>> supplier;

    public BooksHystrixCommand(String name, Supplier<Observable<T>> supplier) {
        super(HystrixCommandGroupKey.Factory.asKey(name));
        this.supplier = supplier;
    }


    @Override
    protected Observable<T> construct() {
        return supplier.get();
    }

}
