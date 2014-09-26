package westrun.exprepo;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Set;

import com.google.common.collect.Sets;

import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.db.Records;
import briefj.opt.OptionsParser;
import briefj.repo.GitRepository;
import briefj.run.ExecutionInfoFiles;

import static briefj.run.ExecutionInfoFiles.*;

/**
 * Indexes the part of the exec that will not change over the life of an execution.
 * E.g. options, start time, code commit, etc.
 * 
 * This makes it very fast to update (once an exec folder is cached in the db, no
 * need to revisit that folder).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class PermanentIndex
{
  
  public static File getBDIndexFile(ExperimentsRepository repo)
  {
    return new File(repo.localExpRepoRoot, "results/.options-index.db");
  }
  
  public static class CorruptIndexException extends RuntimeException
  {
    private static final long serialVersionUID = 1L;

    public CorruptIndexException()
    {
      super("Index out of date. Try wrun-clean");
    }
  }
  
  public static Records getUpdatedIndex()
  {
    ExperimentsRepository repo = ExperimentsRepository.fromWorkingDirParents();
    
    File database = getBDIndexFile(repo);
    Records r = new Records(database);
    File resultsDirectory = new File(repo.localExpRepoRoot, "results/all");
    
    Set<String> cachedPaths = Sets.newHashSet();
    try 
    {
      cachedPaths = r.recordedExecFolders();
    }
    catch (Exception e)
    {
      // do nothing, this might happen e.g. the first time running this function in a repo
    }
    
    // process the new ones
    loop:for (File resultDirectory : BriefFiles.ls(resultsDirectory, "exec"))
    {
      if (cachedPaths.contains(resultDirectory.getAbsolutePath()))
        continue loop;
      
      
      
      File optionsFile = new File(resultDirectory, repo.configuration.getOptionsLocation()); //ExecutionInfoFiles.getFile(ExecutionInfoFiles.OPTIONS_MAP, resultDirectory);
      LinkedHashMap<String, String> keyValuePairs = null;;
      try
      {
        keyValuePairs = OptionsParser.readOptionsFile(optionsFile);
      }
      catch (Exception e)
      {
        continue loop;
      }
      
      System.out.println("Caching " + resultDirectory);
      
      addFileContentsToKeyValuePairs(ExecutionInfoFiles.getFile(START_TIME_FILE, resultDirectory), keyValuePairs);
      addFileContentsToKeyValuePairs(ExecutionInfoFiles.getFile(REPOSITORY_INFO, resultDirectory), keyValuePairs);
      boolean codeDirty = (ExecutionInfoFiles.getFile(REPOSITORY_DIRTY_FILES, resultDirectory).exists());
      keyValuePairs.put(DIRTY_FILE_COLUMN_NAME, "" + codeDirty);
      
      try
      { 
        long commitTime = GitRepository.fromLocal(repo.localCodeRepoRoot).commitTime(keyValuePairs.get("commit"));
        keyValuePairs.put("git_commit_time", "" + commitTime);
      } catch (Exception e)
      {
        
      }
      
      r.recordFullRun(keyValuePairs, resultDirectory);
    }
    
    return r;
  }
  
  public static String DIRTY_FILE_COLUMN_NAME = "dirty_files";
  
  public static void addFileContentsToKeyValuePairs(File file, LinkedHashMap<String, String> keyValuePairs)
  {
    int nLines = 0;
    loop:for (String line : BriefIO.readLines(file))
      if (!line.isEmpty())
      {
        nLines++;
        if (nLines > 1)
          break loop;
      }
    if (nLines < 2)
      keyValuePairs.put(getNameNoExtension(file), BriefIO.fileToString(file));
    else
      keyValuePairs.putAll(OptionsParser.readOptionsFile(file));
      
  }
  
  private static String getNameNoExtension(File file)
  {
    return file.getName().replaceAll("[.].*", "");
  }

  public static void main(String [] args)
  {
    getUpdatedIndex();
  }
}
