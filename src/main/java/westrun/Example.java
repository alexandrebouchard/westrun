package westrun;

import briefj.opt.Option;
import briefj.run.Mains;



public class Example implements Runnable
{
  @Option public double param1 = 1.0;
  @Option public String param2 = "test";
  
  public static void main(String [] args)
  {
    Mains.instrumentedRun(args, new Example());
  }

  @Override
  public void run()
  {
    System.out.println("param1 = " + param1 + " and param2 = " + param2);
  }
}
