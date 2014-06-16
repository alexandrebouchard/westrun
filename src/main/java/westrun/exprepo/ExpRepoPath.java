package westrun.exprepo;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

import briefj.BriefLists;

/**
 * Note: path should be listed in topological order (parents before childre
 * to ensure proper init).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public enum ExpRepoPath
{
  CONFIG              (true,  ".westrun"),
  MAIN_CONFIG_FILE    (false, ".westrun", "config.json"),
  IGNORE_FILE         (false, ".westrun", "syncignore"),
  SYNC_LOG_1          (false, ".westrun", "synclog1"),
  SYNC_LOG_2          (false, ".westrun", "synclog2"),
  CODE_TRANSFERRED    (true,  ".code_transferred"), 
  CODE_TO_TRANSFER    (true,  ".code_to_transfer"), 
  TEMPLATE_DRAFT      (true,  "template_drafts"), 
  TEMPLATE_RUN        (true,  "template_run");
  
  public final boolean isDirectory;
  public final List<String> pathRelativeToExpRepoRoot;
  ExpRepoPath(boolean isDir, String ... path)
  {
    this.isDirectory = isDir;
    this.pathRelativeToExpRepoRoot = Arrays.asList(path);
  }
  public File buildFile(File root)
  {
    for (String item : pathRelativeToExpRepoRoot)
      root = new File(root, item);
    return root;
  }
  public String getName()
  {
    return BriefLists.last(pathRelativeToExpRepoRoot);
  }
  public String getPathRelativeToExpRepoRoot()
  {
    return Joiner.on("/").join(pathRelativeToExpRepoRoot);
  }
}