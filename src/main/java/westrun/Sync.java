package westrun;

import java.io.File;

import westrun.exprepo.ExpRepoPath;
import westrun.exprepo.ExperimentsRepository;

import binc.Command;
import briefj.BriefIO;

import static binc.Command.call;




public class Sync
{
  
  public static void main(String [] args)
  {
    sync(ExperimentsRepository.fromWorkingDirParents());
  }
  
  public static void sync(ExperimentsRepository repo)
  {
    System.out.println("Starting sync. Note: if files were deleted, this may recreate them " +
    		"\n  (use wrun-clean for removing deleted files on the remote host).");
    
    call(rsync(repo.localExpRepoRoot, repo.remoteExpRepoRoot, false, repo.resolveLocal(ExpRepoPath.SYNC_LOG_1), repo.resolveLocal(ExpRepoPath.IGNORE_FILE)));
    call(rsync(repo.remoteExpRepoRoot, repo.localExpRepoRoot, false, repo.resolveLocal(ExpRepoPath.SYNC_LOG_2), repo.resolveLocal(ExpRepoPath.IGNORE_FILE)));
    
    System.out.println("Sync complete. See .westrun/log_exp_push and _pull for details");
  }
  
  public static void pushLocalToRemote(ExperimentsRepository repo)
  {
    System.out.println("Starting to push local state to remote state.");
    
    call(rsync(repo.localExpRepoRoot, repo.remoteExpRepoRoot, true, repo.resolveLocal(ExpRepoPath.SYNC_LOG_1), repo.resolveLocal(ExpRepoPath.IGNORE_FILE)));
    
    System.out.println("Sync complete. See .westrun/log_exp_push for details");
  }
  
  public static void pushCode(ExperimentsRepository repo)
  {
    System.out.println("Starting to push code to remote state.");
    
    File local  = repo.resolveLocal(ExpRepoPath.CODE_TO_TRANSFER);
    File remote = repo.resolveRemote(ExpRepoPath.CODE_TO_TRANSFER);
    
    call(rsync(local, remote, true, repo.resolveLocal(ExpRepoPath.SYNC_CODE), null));
    
    System.out.println("Sync complete. See .westrun/log_code_push for details");
  }
  
  private static Command rsync(File srcDir, File destDir, boolean deleteAfter, File logFile, File exclusions)
  {
    Command result = Command.byName("rsync");
    
    result = result.appendArgs("--update") // skip files that are newer on the receiver" +
                   .appendArgs("--recursive")
                   .appendArgs("--links")
                   .appendArgs("--perms")
                   .appendArgs("--group")
                   .appendArgs("--executability")
                   .appendArgs("--times");
    
    if (deleteAfter)
      result = result.appendArgs("--delete-after");
    
    if (exclusions != null)
      result = result.appendArgs("--exclude-from=" + exclusions.getPath());
    
    result = result.appendArgs(srcDir.getPath() + "/")
                   .appendArgs(destDir.getPath());
    
    if (logFile != null)
      result = result.saveOutputTo(logFile);
    
    return result.throwOnNonZeroReturnCode();
  }
  
  public static void createExcludeList(ExperimentsRepository repo)
  {
    StringBuilder exclude = new StringBuilder();
    
    for (ExpRepoPath path : ExpRepoPath.values())
      exclude.append("/" + path.getPathRelativeToExpRepoRoot() + "\n");
    
    BriefIO.write(repo.resolveLocal(ExpRepoPath.IGNORE_FILE), exclude);
  }
}
