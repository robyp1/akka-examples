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
          sender().tell(Msg.WAIT, self());
          CompletableFuture.supplyAsync( //chiamato con tell che sincrono ma viene eseguito in asincrono così
                  () -> longRunningTask()
          )
          .thenAccept(s -> {//quando finisce il processo esegue questo
              if (LocalTime.now().getSecond() % 2 == 0) {
                  log.info("async process complete, sending DONE! ... ");
                  sender().tell(Msg.DONE, self());
              }
              else throw new ActorRuntimeException("casual error");
          })
          .get();
      })
      .matchEquals(Msg.GREET_RESP, (m) -> { //chiamato con metodo ask, viene eseguito in asincrono (ask lo wrappa in CompletableFuture o Future)
          log.info("Waiting ... ");
          sender().tell(Msg.WAIT, self());
          TaskResult result = new TaskResult(longRunningTask());
          log.info("async process complete, sending result {} ", result.getResult());
          sender().tell(result, getSelf());

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
