package sample.hello;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import javafx.concurrent.Task;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static akka.pattern.Patterns.ask;
import static sample.hello.Greeter.Msg;

public class HelloWorld extends AbstractActor {

    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    ActorRef _greeter;

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
        // create the greeter actor as child of this actor
        _greeter = getContext().actorOf(Props.create(Greeter.class, param), "greeter");
        getContext().watch(_greeter); //ascolto il figlio, quando l'actor greed termina
    }

    @Override
  public Receive createReceive() {
    String s =  MyUtilFunctions.actorInfo.apply(Msg.DONE, this);
    Duration d = Duration.ofSeconds(20);
    log.info("**** send {} to greter with ask  ", Msg.GREET_RESP.name());
    //Pattern.ask() -> fire and get a Future reply (async)
    final CompletionStage<Object> asyncResultResponse = ask(_greeter, Msg.GREET_RESP, d);
    log.debug("enter createReceive for {}", s);
    return receiveBuilder()
            .matchEquals(Msg.DONE, m -> {
              // when the greeter is done, stop this actor and with it the application
              log.info("Received ***{} " , Msg.DONE.name());
              getContext().stop(_greeter);//stop sul child
            })
            .matchEquals(Msg.WAIT, m ->{
              log.info("Received **{},  remote long process running, i'm  waiting for msg done " , Msg.WAIT.name());
                Object resp = asyncResultResponse.toCompletableFuture().get();
                while (! (resp instanceof TaskResult)) {
                    resp = asyncResultResponse.toCompletableFuture().get();
                    log.info("get: " + resp);
                }
                log.info("**** greeter service response is " + resp);
            })
            .match(Terminated.class, t -> {log.info("Terminato greeter {}", t.actor());})
            .build();
        }

  @Override
  public void preStart() throws ExecutionException, InterruptedException {
    log.debug("enter preStart for {}", MyUtilFunctions.actorInfo.apply(Msg.DONE, this));
    // tell it to perform the greeting
    log.info("**send Greet message to greeter**");
    _greeter.tell(Msg.GREET, self()); //-> fire and forget
  }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        log.debug("enter in supervisorStrategy {}", strategy.getClass().getSimpleName() );
        return strategy;
    }

    @Override
    public void postStop() throws Exception {
        log.info("postStop helloWorld actor");
    }
}
