package westrun;

import briefj.BriefIO;
import westrun.exprepo.ExperimentsRepository;



public class Clean
{
  public static void main(String [] args)
  {
    ExperimentsRepository repo = ExperimentsRepository.fromWorkingDirParents();
    
    // TODO: detect if remote jobs are running
    
    System.out.println("Clean should not be used if remote jobs are running.");
    String answer = BriefIO.prompt("Confirm (y/n)");
    
    if (!"y".equals(answer))
      System.exit(1);
    
    Sync.pushLocalToRemote(repo);
    
    // TODO: - erase contents of transferred code dir
    //       - create a fresh one
    //       - change the links in the results to point to the new copy
 
  }
}
