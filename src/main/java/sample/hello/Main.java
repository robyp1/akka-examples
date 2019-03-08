package sample.hello;



public class Main {

  public static void main(String[] args) throws InterruptedException {
    akka.Main.main(new String[] { HelloWorld.class.getName() });
  }
}
