package westrun;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import westrun.exprepo.PermanentIndex;
import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.db.Records;
import briefj.opt.Option;
import briefj.run.Mains;
import briefj.run.OptionsUtils;
import briefj.run.Results;


/**
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Collect implements Runnable
{
  // not an array to mirror sql syntax more closely, e.g. select a,b
  @Option(gloss = "Note: only fields available in wrun-search are available for efficiency reasons.")
  public String select = Records.FOLDER_LOCATION_COLUMN;
  
  @Option(gloss = "Note: only constraints on the fields available in wrun-search are available for efficiency reasons")
  public String where = "";
  
  @Option
  public String csvFile = "";
  
  @Option
  public ArrayList<String> mapFiles = new ArrayList<>();
  
  @Option(gloss = "A simple file is a file where the key is the file name (extension-stripped), and the value is the contents")
  public ArrayList<String> simpleFiles = new ArrayList<>();
  
  @Option(gloss = "If true, write results to a unique execution directory")
  public boolean save = false;
  
  @Option
  public boolean pipe = true;
  
  private Records output;

  private File databaseFile;
  
  @Override
  public void run()
  {
    if (!save && !pipe)
    {
      System.err.println("Select one of the two modes: -save or -pipe");
      System.exit(1);
    }
    
    if (!select.contains(Records.FOLDER_LOCATION_COLUMN))
      select = select + "," + Records.FOLDER_LOCATION_COLUMN;
    
    databaseFile = save ? Results.getFileInResultFolder("db.sqlite") : BriefFiles.createTempFile();
    output =  new Records(databaseFile);  
    
    Records records = PermanentIndex.getUpdatedIndex();
    try
    {
      ResultSet results = records.select(select, where);
      while (results.next())
      {
        LinkedHashMap<String,String> globalKeyValuePairs = new LinkedHashMap<>();
        ResultSetMetaData metaData = results.getMetaData();
        for (int i = 0; i < metaData.getColumnCount(); i++)
          globalKeyValuePairs.put(metaData.getColumnName(i+1), results.getString(metaData.getColumnName(i+1)));
        File directory = new File(results.getString(Records.FOLDER_LOCATION_COLUMN));
        for (String mapFileName : mapFiles)
        {
          File mapFile = new File(directory, mapFileName);
          if (mapFile.exists())
            PermanentIndex.addMapFileToKeyValuePairs(mapFile, globalKeyValuePairs);
        }
        for (String simpleFileName : simpleFiles)
        {
          File simpleFile = new File(directory, simpleFileName);
          if (simpleFile.exists())
            PermanentIndex.addSimpleFileContentsToKeyValuePairs(simpleFile, globalKeyValuePairs);
        }
        if (!StringUtils.isEmpty(csvFile))
        {
          File csvFileAbsPath = new File(directory, csvFile);
          if (csvFileAbsPath.exists())
            for (Map<String,String> csvLine : BriefIO.readLines(csvFileAbsPath).indexCSV())
            {
              csvLine.putAll(globalKeyValuePairs);
              
              output.record((LinkedHashMap<String, String>)csvLine);
            }
        }
        else
          output.record(globalKeyValuePairs);
      }
      
      if (pipe)
      {
        byte[] data = Files.readAllBytes(databaseFile.toPath());
        System.out.write(data);
      }
    } 
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    } 
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static void main(String [] args) 
  {
    try
    {
      Collect collect = new Collect();
      OptionsUtils.parseOptions(args, collect);
      if (collect.save)
        Mains.instrumentedRun(args, collect);
      else 
        collect.run();
    }
    catch (Exception e)
    {
      System.err.println(e);
    }
  }
}
