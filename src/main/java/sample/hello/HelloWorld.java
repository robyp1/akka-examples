package sample.hello;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static akka.pattern.Patterns.ask;
import static sample.hello.Greeter.Msg;

public class HelloWorld extends AbstractActor {

    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    //strategia di bulkahead per il solo child che fallissce (OneForOneStrategy)
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    10,
                    Duration.ofMinutes(1),
                    DeciderBuilder.match(ActorRuntimeException.class, e -> SupervisorStrategy.restart())
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());
    private final Integer param;

    public HelloWorld(Integer param) {
        this.param = param;
    }

    @Override
  public Receive createReceive() {
    String s =  MyUtilFunctions.actorInfo.apply(Msg.DONE, this);
    log.debug("enter createReceive for {}", s);
    return receiveBuilder()
            .matchEquals(Msg.DONE, m -> {
              // when the greeter is done, stop this actor and with it the application
              log.info("Received {} " , Msg.DONE.name());
              //getContext().stop(self());
            })
            .matchEquals(Msg.WAIT, m ->{
              log.info("Received {},  remote long process running, i'm  waiting for msg done " , Msg.WAIT.name());
            })
            .match(TaskResult.class, r -> {
                log.info("Received {} " , r);
                //getContext().stop(self());
            } )
            .build();
        }

  @Override
  public void preStart() throws ExecutionException, InterruptedException {
    log.debug("enter preStart for {}", MyUtilFunctions.actorInfo.apply(Msg.DONE, this));
    // create the greeter actor
    final ActorRef greeter = getContext().actorOf(Props.create(Greeter.class, param), "greeter");
    // tell it to perform the greeting
    log.info("**send Greet message to greeter**");
    greeter.tell(Msg.GREET, self()); //-> fire and forget (asyncronous)
      //asincrono con timeout:
    //Pattern.ask() -> fire and get a Future reply (async)
      final Duration t = Duration.ofSeconds(20);
      log.info("**** send {} to greter with ask  ",Msg.GREET_RESP.name());
      CompletionStage<Object> asyncResultResponse = ask(greeter, Msg.GREET_RESP, t);
      CompletableFuture<Object> result = asyncResultResponse.toCompletableFuture();
      TaskResult r = (TaskResult) result.get();
      log.info("**** greete r service response is " + r);
    // GREET_RESP
  }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        log.debug("enter in supervisorStrategy {}", strategy.getClass().getSimpleName() );
        return strategy;
    }


}
