package westrun;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;
import westrun.exprepo.PermanentIndex;
import westrun.exprepo.PermanentIndex.CorruptIndexException;
import briefj.db.Records;
import briefj.opt.Option;
import briefj.run.OptionsUtils;
import briefj.run.OptionsUtils.InvalidOptionsException;



/**
 * Utility to search (and index) experiments. 
 * 
 * For performance reasons, this only indexes the parts of runs that will not change,
 * e.g. command line options, git coordinates, directory, etc.
 * 
 * See Collect for tools combining the output of different runs.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
public class Search implements Runnable
{
  // not an array to mirror sql syntax more closely, e.g. select a,b
  @Option 
  public String select = "";
  
  @Option
  public String constraints = "";
  
  @Override
  public void run()
  {
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
          ResultSetMetaData metaData = results.getMetaData();
          for (int i = 0; i < metaData.getColumnCount(); i++)
            System.out.print(results.getString(metaData.getColumnName(i+1)) + "\t");
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
