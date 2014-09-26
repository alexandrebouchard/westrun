package westrun;

import briefj.db.Records;
import briefj.run.Mains;
import westrun.exprepo.ExperimentsRepository;
import westrun.exprepo.PermanentIndex;
import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;




public class Search implements Runnable
{
  
  
  private ExperimentsRepository repo;

  @Override
  public void run()
  {
    repo = ExperimentsRepository.fromWorkingDirParents();
    
    showColumns();
  }

  private void showColumns()
  {
    Records records = PermanentIndex.getUpdatedIndex();
    
    for (String columnName : records.getCurrentCols())
      System.out.println(columnName);
  }
  
  public static void main(String [] args) 
  {
    try
    {
      Mains.instrumentedRun(args, new Launch());
    }
    catch (NotInExpRepoException niere)
    {
      System.err.println(niere.getMessage());
    }
  }
}
