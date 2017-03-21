
import io.vertx.redis.RedisClient;


def eb = vertx.eventBus()

def PREFIX = "inventory:"


def increaseConsumer = eb.consumer("inventory.storage.increase")
increaseConsumer.handler({ message ->
        def body =  message.body();
        
        def bookId = body.getLong("bookId")
        def amount = body.getInteger("amount")

        
        
        def redis = RedisClient.create(vertx)
        redis.incrby(PREFIX+bookId, amount, { ar-> 
                if(ar.succeeded()) {
                    message.reply("increased: "+ar.result())
                } else {
                    message.reply(ar.cause().getMessage())
                }
            })
    })

def decreaseConsumer = eb.consumer("inventory.storage.decrease")
decreaseConsumer.handler({ message ->
        def body =  message.body();
        
        def bookId = body.getLong("bookId")
        def amount = body.getInteger("amount")

        
        
        def redis = RedisClient.create(vertx, vertx.getOrCreateContext().config())
        redis.decrby(PREFIX+bookId, amount, { ar-> 
                if(ar.succeeded()) {
                    message.reply("decreased: "+ar.result())
                } else {
                    message.reply(ar.cause().getMessage())
                }
            })
    })

def balanceConsumer = eb.consumer("inventory.storage.balance")
balanceConsumer.handler({ message ->
        def body =  message.body();
        
        def bookId = body.getLong("bookId")

        println "received request for ${bookId}"
        
        
        def redis = RedisClient.create(vertx, vertx.getOrCreateContext().config())
        redis.get(PREFIX+bookId, { ar-> 
                if(ar.succeeded()) {
                    message.reply("get: "+ar.result())
                } else {
                    message.reply(ar.cause().getMessage())
                }
            })
    })