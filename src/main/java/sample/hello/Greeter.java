package sample.hello;

import akka.actor.AbstractActor;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import scala.concurrent.AwaitPermission$;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class Greeter extends AbstractActor {

  final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Integer param;

    //dispatcher alternativo per evitare starvation nel thread asincrono, dove si evita il get
    ExecutionContext ec = getContext().getSystem().dispatchers().lookup("my-blocking-dispatcher");

    public static enum Msg {
    GREET, DONE, WAIT, GREET_RESP ;
  }

    public Greeter(Integer param) {
      this.param = param;
    }

    @Override
  public Receive createReceive() {
    log.debug("enter createReceive for {}",  MyUtilFunctions.actorInfo.apply(Msg.GREET, this));

    return receiveBuilder()
        .matchEquals(Msg.GREET, m -> {
            Future<Msg> future = Futures.future( //chiamato con tell che sincrono ma viene eseguito in asincrono wrappandolo nel Futures
                    () -> {
                        log.info("Waiting 1... ");
                        sender().tell(Msg.WAIT, self()); //mando il messaggio di attesa, che sto elaborando
                        Boolean result = longRunningTask();
                        log.info("param: {} async process complete ", param.intValue());
                        log.info("Sending DONE");
                        sender().tell(Msg.DONE, self());
                        return Msg.DONE;
                    }, param == 2 ? getContext().getDispatcher() : ec);//switch from default dispatcher to specialized one..
            Msg result = future.result(Duration.apply(5000L, TimeUnit.SECONDS), AwaitPermission$.MODULE$);
//            log.info("Sending DONE");
            //sender().tell(Msg.DONE, self());
            log.info("*************************");
        })
        .matchEquals(Msg.GREET_RESP, (m) -> { //chiamato con metodo ask, viene eseguito in asincrono (ask lo wrappa in CompletableFuture o Future?)
            log.info("Waiting 2... ");
//        non mando WAIT come l'altro perchè l'ask restituisce il primo tell fatto quindi wait lo evito(siccome result andrebbe come secondo tell, e non andrebbe a buon fine)
            TaskResult result = new TaskResult(longRunningTask());
            log.info("async process complete, sending result {} ", result);
            sender().tell(result, getSelf());//questo l'unico tell che è la risposta all'esecuzione asincrona

        })
         .build();
  }

    private Boolean longRunningTask() {
        log.info("Run async ... ");
        try {
            Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    @Override
    public void postStop() throws Exception {
        log.info("postStop greeter actor");
    }


}
