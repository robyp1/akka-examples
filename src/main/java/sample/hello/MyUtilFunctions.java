package sample.hello;

import akka.actor.AbstractActor;

import java.util.function.BiFunction;

public class MyUtilFunctions {

    final static BiFunction<Enum,AbstractActor,String> actorInfo = (m, a) -> {
        a.getContext().watch(a.getSelf());
        StringBuilder sbt = new StringBuilder();
        sbt.append("typeOf Message selector : " + m.getDeclaringClass().getName() +"."  + m.name())
                .append(" " + a.getSelf().path()).append(" - parent path: ");
        a.getContext().getSelf().path().getElements().forEach(
                actor -> {
                    sbt.append(actor).append("/");
                }
        );
        return sbt.toString().substring(0, sbt.toString().lastIndexOf("/"));
    };
}