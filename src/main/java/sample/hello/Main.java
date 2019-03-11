package sample.hello;


import akka.actor.ActorSystem;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws InterruptedException, IOException {

    //akka.Main.main(new String[] { HelloWorld.class.getName() });
    ActorSystem iotSystem = ActorSystem.create("iot-system");
    try {
      iotSystem.actorOf(IoTSupervisor.getProprops(), "iot-supervisor");
      //System.out.println(System.getProperty("java.io.tmpdir"));
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally
    {
      iotSystem.terminate();
    }
  }
}
