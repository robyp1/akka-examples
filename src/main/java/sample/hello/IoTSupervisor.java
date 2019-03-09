package sample.hello;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;
import java.util.concurrent.CompletionException;

public class IoTSupervisor extends AbstractActor {

    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

       //strategia di bulkahead per il solo child che fallissce (OneForOneStrategy)
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    10,
                    Duration.ofMinutes(1),
                    DeciderBuilder.match(ActorRuntimeException.class, e -> SupervisorStrategy.restart())
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());

    private final Integer param1;

    public IoTSupervisor(Integer param) {
        this.param1= param;
        System.out.println("start with param " + param);
        ActorRef helloWorld = getContext().actorOf(Props.create(HelloWorld.class,  param), "hello-World");
    }

    public static Props getProprops(){
        return  Props.create(IoTSupervisor.class, new Integer(2));//il secondo argomento viene passato al costruttore
    }

    @Override
    public void preStart() throws Exception {
        log.info("IoT Application started");
    }

    @Override
    public void postStop() throws Exception {
        log.info("IoT Application stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }


    @Override
    public SupervisorStrategy supervisorStrategy() {
        log.debug("enter in supervisorStrategy {}", strategy.getClass().getSimpleName() );
        return strategy;
    }
}
