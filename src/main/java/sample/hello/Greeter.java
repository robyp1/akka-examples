package sample.hello;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

public class Greeter extends AbstractActor {

  final LoggingAdapter log = Logging.getLogger(getContext().system(), this);


  public static enum Msg {
    GREET, DONE, WAIT, GREET_RESP ;
  }

  @Override
  public Receive createReceive() {
    log.debug("enter createReceive for {}",  MyUtilFunctions.actorInfo.apply(Msg.GREET, this));
    return receiveBuilder()
      .matchEquals(Msg.GREET, m -> {
          log.info("Waiting ... ");
          sender().tell(Msg.WAIT, self()); //mando il messaggio di attesa, che sto elaborando
          CompletableFuture.supplyAsync( //chiamato con tell che sincrono ma viene eseguito in asincrono così
                  () -> longRunningTask()
          )
          .thenAccept(s -> {//quando finisce il processo esegue questo
              if (LocalTime.now().getSecond() % 2 == 0) { //casualità per provare il bulk ahead e restart del processo in failure
                  log.info("async process complete, sending DONE! ... ");
                  sender().tell(Msg.DONE, self());
              }
              else throw new ActorRuntimeException("casual error");
          })
          .get();
      })
      .matchEquals(Msg.GREET_RESP, (m) -> { //chiamato con metodo ask, viene eseguito in asincrono (ask lo wrappa in CompletableFuture o Future?)
          log.info("Waiting ... ");
//       non mando WAIT come l'altro perchè l'ask restituisce il primo tell fatto quindi wait lo evito(siccome result andrebbe come secondo tell, e non andrebbe a buon fine)
          TaskResult result = new TaskResult(longRunningTask());
          log.info("async process complete, sending result {} ", result.getResult());
          sender().tell(result, getSelf());//questo l'unico tell che è la risposta all'esecuzione asincrona

      })
      .build();
  }

    private Boolean longRunningTask() {
        log.info("Run async ... ");
        try {
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


}
