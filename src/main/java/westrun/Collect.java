package westrun;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import westrun.exprepo.PermanentIndex;
import briefj.BriefIO;
import briefj.BriefLog;
import briefj.db.Records;
import briefj.opt.Option;
import briefj.run.OptionsUtils;


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
  public String constraints = "";
  
  @Option(required = true, gloss = "Comma separated fields to output, could be either from select, and/or csv/map/simple files")
  public String print;
  
  @Option
  public String csvFile = "";
  
  @Option
  public ArrayList<String> mapFiles = new ArrayList<>();
  
  @Option(gloss = "A simple file is a file where the key is the file name (extension-stripped), and the value is the contents")
  public ArrayList<String> simpleFiles = new ArrayList<>();
  
  @Option(gloss = "Database to output to, otherwise, the result is printed to standard out.")
  public File outputDatabase = null;

  private Records output;
  private LinkedHashSet<String> printSet;

  @Override
  public void run()
  {
    if (!select.contains(Records.FOLDER_LOCATION_COLUMN))
      select = select + "," + Records.FOLDER_LOCATION_COLUMN;
    
    output = outputDatabase == null ? null : new Records(outputDatabase);
    printSet = new LinkedHashSet<String>(Arrays.asList(print.split("\\s*,\\s*")));
    
    Records records = PermanentIndex.getUpdatedIndex();
    ResultSet results = records.select(select, constraints);
    try
    {
      while (results.next())
      {
        LinkedHashMap<String,String> globalKeyValuePairs = new LinkedHashMap<>();
//        for (String colName : select.split("\\s*,\\s*"))
        ResultSetMetaData metaData = results.getMetaData();
        for (int i = 0; i < metaData.getColumnCount(); i++)
          globalKeyValuePairs.put(metaData.getColumnName(i+1), results.getString(metaData.getColumnName(i+1)));
        File directory = new File(results.getString(Records.FOLDER_LOCATION_COLUMN));
        for (String mapFileName : mapFiles)
        {
          File mapFile = new File(directory, mapFileName);
          if (mapFile.exists())
            PermanentIndex.addMapFileToKeyValuePairs(mapFile, globalKeyValuePairs);
          else
            BriefLog.warnOnce("Warning: " + mapFileName + " skipped at least once (e.g. " + mapFile + " does not exist)");
        }
        for (String simpleFileName : simpleFiles)
        {
          File simpleFile = new File(directory, simpleFileName);
          if (simpleFile.exists())
            PermanentIndex.addSimpleFileContentsToKeyValuePairs(simpleFile, globalKeyValuePairs);
          else
            BriefLog.warnOnce("Warning: " + simpleFile + " skipped at least once (e.g. " + simpleFile + " does not exist)");
        }
        if (!StringUtils.isEmpty(csvFile))
        {
          File csvFileAbsPath = new File(directory, csvFile);
          if (csvFileAbsPath.exists())
            for (Map<String,String> csvLine : BriefIO.readLines(csvFileAbsPath).indexCSV())
            {
              csvLine.putAll(globalKeyValuePairs);
              output((LinkedHashMap<String, String>)csvLine);
            }
          else
            BriefLog.warnOnce("Warning: " + csvFile + " skipped at least once (e.g. " + csvFileAbsPath + " does not exist)");
        }
        else
          output(globalKeyValuePairs);
      }
    } 
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  private void output(LinkedHashMap<String, String> keyValuePairs)
  {
    keyValuePairs.keySet().retainAll(printSet);
    if (output == null)
    {
      for (String key : printSet) 
        System.out.print(keyValuePairs.get(key) + "\t");
      System.out.println();
      // Using above strategy to preserve requested order when printing
      // System.out.println(Joiner.on("\t").join(keyValuePairs.values()));
    }
    else
      output.record(keyValuePairs);
  }

  public static void main(String [] args) 
  {
    try
    {
      Collect collect = new Collect();
      OptionsUtils.parseOptions(args, collect);
      collect.run();
    }
    catch (Exception e)
    {
      System.err.println(e);
    }
  }
}
