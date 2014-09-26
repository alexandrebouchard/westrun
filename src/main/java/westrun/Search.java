package westrun;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import briefj.db.Records;
import briefj.opt.Option;
import briefj.run.Mains;
import briefj.run.OptionsUtils;
import briefj.run.OptionsUtils.InvalidOptionsException;
import westrun.exprepo.ExperimentsRepository;
import westrun.exprepo.PermanentIndex;
import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;
import westrun.exprepo.PermanentIndex.CorruptIndexException;




public class Search implements Runnable
{
  @Option 
  public String select = "";
  
  @Option
  public String constraints = "";
  
  private ExperimentsRepository repo;

  @Override
  public void run()
  {
    repo = ExperimentsRepository.fromWorkingDirParents();
    
    if (StringUtils.isEmpty(select))
    {
      System.out.println("Use -select to pick one or several of these columns (space separated).");
      showColumns();
    }
    else
    {
      Records records = PermanentIndex.getUpdatedIndex();
      ResultSet results = records.select(select, constraints);
      try
      {
        while (results.next())
        {
          for (String colName : select.split("\\s*,\\s*"))
            System.out.print(results.getString(colName) + "\t");
          System.out.println();
        }
      } 
      catch (SQLException e)
      {
        throw new RuntimeException(e);
      }
    }
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
      Search search = new Search();
      OptionsUtils.parseOptions(args, search);
      search.run();
    }
    catch (NotInExpRepoException niere)
    {
      System.err.println(niere.getMessage());
    }
    catch (CorruptIndexException cie)
    {
      System.err.println(cie.getMessage());
    }
    catch (InvalidOptionsException ioe)
    {
      
    }
  }
}
