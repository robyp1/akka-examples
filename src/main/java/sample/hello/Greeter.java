package sample.hello;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Greeter extends AbstractActor {

  final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Integer param;


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
          try {
              CompletableFuture.supplyAsync( //chiamato con tell che sincrono ma viene eseguito in asincrono così
                      () -> {
                          log.info("Waiting 1... ");
                          sender().tell(Msg.WAIT, self()); //mando il messaggio di attesa, che sto elaborando
                          return longRunningTask();
                      }
              )
              .thenAccept(s -> {//quando finisce il processo esegue questo
                  if (LocalTime.now().getSecond() % param.intValue() == 0) { //casualità per provare il bulk ahead e restart del processo in failure
                      log.info("param: {} async process complete, sending DONE! ... ",param.intValue());
                      sender().tell(Msg.DONE, self());
                  } else
                      throw new ActorRuntimeException("casual error");
              });
              //.get(); //non blocco perchè non aspetto risultato
              log.info("************************************");
          }catch (ActorRuntimeException ex){
              throw new CompletionException(ex.getCause());
          }
      })
      .matchEquals(Msg.GREET_RESP, (m) -> { //chiamato con metodo ask, viene eseguito in asincrono (ask lo wrappa in CompletableFuture o Future?)
          log.info("Waiting 2... ");
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
