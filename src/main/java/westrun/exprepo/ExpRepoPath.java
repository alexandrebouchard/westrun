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
  LOG_PUSH            (false, ".westrun", "logPush"),
  LOG_PULL            (false, ".westrun", "logPull"),
  LOG_CODE_PUSH       (false, ".westrun", "logCodePush"),
  CODE_TRANSFERRED    (true,  ".codeTransferred"), 
  CODE_TO_TRANSFER    (true,  ".codeToTransfer"), 
  TEMPLATE_DRAFTS     (true,  "templateDrafts"), 
  TEMPLATE_EXECUTED   (true,  "templateExecuted");
  
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