package westrun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.ByteStreams;

import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;
import westrun.exprepo.PermanentIndex;
import westrun.exprepo.PermanentIndex.CorruptIndexException;
import briefj.BriefFiles;
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
  @Option 
  public String select = "";
  
  @Option
  public boolean selectAll = false;
  
  @Option
  public String where = "";
  
  @Option(gloss = "Either: (i) if nothing, then use default database, or (ii) a db file. The -pipe argument takes priority")
  public File db = null;
  
  @Option(gloss = "Whether to get a db from the stdin")
  public boolean pipe = false;

  private Records records;
  
  @Option 
  public boolean showHeader = true;
  
  @Option
  public String delimiter = "\t";
  
  @Override
  public void run()
  {
    if (selectAll)
      select = "*";
    
    records = getRecords(); 
    if (StringUtils.isEmpty(select))
    {
      System.out.println("Use -select to pick one or several of these columns (space separated).");
      showColumns();
    }
    else
    {
      ResultSet results = records.select(select, where);
      try
      {
        ResultSetMetaData metaData = results.getMetaData();
        if (showHeader)
        {
          for (int i = 0; i < metaData.getColumnCount(); i++)
            System.out.print(metaData.getColumnName(i+1) + (i == metaData.getColumnCount() - 1? "" : delimiter));
          System.out.println();
        }
        while (results.next())
        {
          
          for (int i = 0; i < metaData.getColumnCount(); i++)
            System.out.print(results.getString(metaData.getColumnName(i+1)) + (i == metaData.getColumnCount() - 1? "" : delimiter));
          System.out.println();
        }
      } 
      catch (SQLException e)
      {
        throw new RuntimeException(e);
      }
    }
  }

  private Records getRecords()
  {
    try
    {
      if (pipe)
      {
        byte[] data = ByteStreams.toByteArray(System.in);
        File temp = BriefFiles.createTempFile();
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(data);
        fos.close();
        return new Records(temp);
      }
      else if (db == null)
        return PermanentIndex.getUpdatedIndex();
      else 
        return new Records(db);
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  private void showColumns()
  {
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
