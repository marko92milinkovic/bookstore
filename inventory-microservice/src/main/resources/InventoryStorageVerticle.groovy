import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.redis.RedisClient
import io.vertx.rx.java.ObservableHandler
import io.vertx.rx.java.RxHelper
import rx.observers.Observers
import rx.Observer


def eb = vertx.eventBus()

def PREFIX = "inventory:"
def redis = RedisClient.create(vertx, vertx.getOrCreateContext().config())


eb.consumer("inventory.storage.increase").handler({ message ->
    def body = message.body()

    def bookId = body.getLong("bookId")
    def amount = body.getInteger("amount")

    redis.incrby(PREFIX + bookId, amount, { ar ->
        if (ar.succeeded()) {
            message.reply(ar.result())
        } else {
            message.reply(ar.cause().getMessage())
        }
    })
})

eb.consumer("inventory.storage.decrease").handler({ message ->
    def body = message.body()

    def bookId = body.getLong("bookId")
    def amount = body.getInteger("amount")

    redis.decrby(PREFIX + bookId, amount, { ar ->
        if (ar.succeeded()) {
            message.reply(ar.result())
        } else {
            message.reply(ar.cause().getMessage())
        }
    })
})

eb.consumer("inventory.storage.balance").handler({ message ->
    def body = message.body()

    def bookId = body.getLong("bookId")

    println "received request for ${bookId}"


    redis.get(PREFIX + bookId, { ar ->
        if (ar.succeeded()) {
            message.reply(ar.result())
        } else {
            message.reply(ar.cause().getMessage())
        }
    })
})


//ObservableHandler<Message> observable = RxHelper.observableHandler();
//observable.subscribe({ message ->
//    // Fired
//    def bookId = message.body().getLong("bookId")
//    //....
//    int amount = getResult();
//    message.reply(amount);
//});
//Handler<Message> messageHandler = observable.toHandler()
//
//def balanceConsumer = eb.consumer("inventory.storage.balance").handler(messageHandler)
