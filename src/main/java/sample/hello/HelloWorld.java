package sample.hello;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

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

  @Override
  public Receive createReceive() {
    String s =  MyUtilFunctions.actorInfo.apply(Msg.DONE, this);
    log.debug("enter createReceive for {}", s);
    return receiveBuilder()
            .matchEquals(Msg.DONE, m -> {
              // when the greeter is done, stop this actor and with it the application
              log.info("Received {}, stop actor " , Msg.DONE.name());
              getContext().stop(self());
            })
            .matchEquals(Msg.WAIT, m ->{
              log.info("Received {},  actor wait for msg done " , Msg.WAIT.name());
            })
            .build();
        }

  @Override
  public void preStart() {
    log.debug("enter preStart for {}", MyUtilFunctions.actorInfo.apply(Msg.DONE, this));
    // create the greeter actor
    final ActorRef greeter = getContext().actorOf(Props.create(Greeter.class), "greeter");
    // tell it to perform the greeting
    greeter.tell(Msg.GREET, self());
  }



  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
}
