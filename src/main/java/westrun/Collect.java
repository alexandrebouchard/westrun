package westrun;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
  @Option 
  public String select = "*";
  
  @Option(gloss = "Note: only constraints on the fields available in wrun-search are available for efficiency reasons")
  public String where = "";
  
  @Option
  public String csvFile = "";
  
  @Option
  public ArrayList<String> mapFiles = new ArrayList<>();
  
  @Option(gloss = "A simple file is a file where the key is the file name (extension-stripped), and the value is the contents")
  public ArrayList<String> simpleFiles = new ArrayList<>();
  
  @Option(gloss = "If true, write results to a unique execution directory and print where things were saved. "
      + "If false, output the DB to standard out to be piped into wrun-search")
  public boolean save = false;
  
  private Records output;

  private File databaseFile;

  @Override
  public void run()
  {
    try
    {
      // read all the info from the records up front and close that db since
      // it is not that large, and the loop below can be slow sometimes
      // in which case we want to still be able to do other operations 
      // (sqlite does not like two processes using the db)
      Queue<LinkedHashMap<String,String>> indexData = new LinkedList<>();
      {
        Records records = PermanentIndex.getUpdatedIndex();
        ResultSet results = records.select(select, where);
        while (results.next())
        {
          LinkedHashMap<String,String> globalKeyValuePairs = new LinkedHashMap<String, String>();
          
          ResultSetMetaData metaData = results.getMetaData();
          for (int i = 0; i < metaData.getColumnCount(); i++)
              globalKeyValuePairs.put(metaData.getColumnName(i+1), results.getString(metaData.getColumnName(i+1)));
          indexData.add(globalKeyValuePairs);
        }
        records.close();
      }
      
      databaseFile = save ? Results.getFileInResultFolder("db.sqlite") : BriefFiles.createTempFile();
      output =  new Records(databaseFile);
      output.conn.setAutoCommit(false);
      loop : while (!indexData.isEmpty())
      {
        // note: not keeping these in the indexData important as they might get inflated by map files and simple files
        LinkedHashMap<String,String> globalKeyValuePairs = indexData.poll();

        globalKeyValuePairs.remove("id");
        
        String directoryString = globalKeyValuePairs.get(Records.FOLDER_LOCATION_COLUMN);
        if (directoryString == null)
        {
          System.err.println("Skipping malformed exec folder");
          continue loop;
        }
        File directory = new File(directoryString);
        for (String mapFileName : mapFiles)
        {
          File mapFile = new File(directory, mapFileName);
          PermanentIndex.addMapFileToKeyValuePairs(mapFile, globalKeyValuePairs);
        }
        for (String simpleFileName : simpleFiles)
        {
          File simpleFile = new File(directory, simpleFileName);
          PermanentIndex.addSimpleFileContentsToKeyValuePairs(simpleFile, globalKeyValuePairs);
        }
        if (!StringUtils.isEmpty(csvFile))
        {
          File csvFileAbsPath = new File(directory, csvFile);
          
          if (csvFileAbsPath.exists())
            for (Map<String,String> csvLine : BriefIO.readLines(csvFileAbsPath).indexCSV())
            {
              csvLine.putAll(globalKeyValuePairs);
              LinkedHashMap<String, String> csvLined = new LinkedHashMap<String, String>(csvLine);
              output.record(csvLined);
            }
        }
        else
          output.record(globalKeyValuePairs);
      }
      output.conn.commit();
      output.close();
      if (!save)
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
    Collect collect = new Collect();
    OptionsUtils.parseOptions(args, collect);
    if (collect.save)
      Mains.instrumentedRun(args, collect);
    else 
      collect.run();
  }
}
