package westrun;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import westrun.exprepo.ExperimentsRepository;
import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;
import westrun.exprepo.PermanentIndex;
import westrun.exprepo.PermanentIndex.CorruptIndexException;
import briefj.db.Records;
import briefj.opt.Option;
import briefj.run.OptionsUtils;
import briefj.run.OptionsUtils.InvalidOptionsException;




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
    List<String> columns = new ArrayList<>();
    for (String columnName : records.getCurrentCols())
      columns.add(columnName);
    Collections.sort(columns);
    for (String columnName : columns)
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
