package westrun;

import java.io.File;

import westrun.exprepo.ExpRepoPath;
import westrun.exprepo.ExperimentsRepository;
import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;

import binc.Command;
import briefj.BriefIO;

import static binc.Command.call;




public class Sync
{
  
  public static void main(String [] args)
  {
    try
    {
      sync(ExperimentsRepository.fromWorkingDirParents());
    }
    catch (NotInExpRepoException niere)
    {
      System.err.println(niere.getMessage());
    }
    
  }
  
  public static void sync(ExperimentsRepository repo)
  {
    System.out.println("Starting sync. Note: if files were deleted, this may recreate them " +
    		"\n  (use wrun-clean for removing deleted files on the remote host).");
    
    call(rsync(null, repo.localExpRepoRoot, repo.sshRemoteHost, repo.remoteExpRepoRoot, false, repo.resolveLocal(ExpRepoPath.LOG_PUSH), repo.resolveLocal(ExpRepoPath.IGNORE_FILE)));
    call(rsync(repo.sshRemoteHost, repo.remoteExpRepoRoot, null, repo.localExpRepoRoot, false, repo.resolveLocal(ExpRepoPath.LOG_PULL), repo.resolveLocal(ExpRepoPath.IGNORE_FILE)));
    
    System.out.println("Sync complete. See " + ExpRepoPath.LOG_PUSH.getPathRelativeToExpRepoRoot() + " and pull for details");
  }
  
  public static void pushLocalToRemote(ExperimentsRepository repo)
  {
    System.out.println("Starting to push local state to remote state.");
    
    call(rsync(null, repo.localExpRepoRoot, repo.sshRemoteHost,  repo.remoteExpRepoRoot, true, repo.resolveLocal(ExpRepoPath.LOG_PUSH), repo.resolveLocal(ExpRepoPath.IGNORE_FILE)));
    
    System.out.println("Sync complete. See " + ExpRepoPath.LOG_PUSH.getPathRelativeToExpRepoRoot() + " for details");
  }
  
  public static void pushCodeTransferred(ExperimentsRepository repo)
  {
    call(rsync(null, repo.resolveLocal(ExpRepoPath.CODE_TRANSFERRED), repo.sshRemoteHost, repo.resolveRemote(ExpRepoPath.CODE_TRANSFERRED), true, null, null));
  }
  
  public static void pushCode(ExperimentsRepository repo)
  {
    System.out.println("Starting to push code to remote state.");
    
    File local  = repo.resolveLocal(ExpRepoPath.CODE_TO_TRANSFER);
    File remote = repo.resolveRemote(ExpRepoPath.CODE_TO_TRANSFER);
    
    call(rsync(null, local, repo.sshRemoteHost, remote, true, repo.resolveLocal(ExpRepoPath.LOG_CODE_PUSH), null));
    
    System.out.println("Sync complete. See " + ExpRepoPath.LOG_CODE_PUSH.getPathRelativeToExpRepoRoot() + " for details");
  }
  
  private static Command rsync(
      String srcHost,
      File srcDir,
      String destHost,
      File destDir, 
      boolean deleteAfter, 
      File logFile, 
      File exclusions)
  {
    Command result = Command.byName("rsync");
    
    result = result.appendArgs("--update") // skip files that are newer on the receiver" +
                   .appendArgs("--recursive")
//                   .appendArgs("--links")
                   .appendArgs("--perms")
                   .appendArgs("--group")
                   .appendArgs("--executability")
                   .appendArgs("--times");
    
    if (deleteAfter)
      result = result.appendArgs("--delete-after");
    
    if (exclusions != null)
      result = result.appendArgs("--exclude-from=" + exclusions.getPath());
    
    srcHost  = srcHost  == null ? "" : (srcHost  + ":");
    destHost = destHost == null ? "" : (destHost + ":");
    
    result = result.appendArgs(srcHost + srcDir.getPath() + "/")
                   .appendArgs(destHost + destDir.getPath());
    
    if (logFile != null)
      result = result.saveOutputTo(logFile);
    
    return result.throwOnNonZeroReturnCode();
  }
  
  public static void createExcludeList(ExperimentsRepository repo)
  {
    StringBuilder exclude = new StringBuilder();
    
    for (ExpRepoPath path : ExpRepoPath.values())
      if (path != ExpRepoPath.PLANS) // do transfer the plans (they contain the scripts)
        exclude.append("/" + path.getPathRelativeToExpRepoRoot() + "\n");
    
//    exclude.append("/*/latest");
    
    BriefIO.write(repo.resolveLocal(ExpRepoPath.IGNORE_FILE), exclude);
  }
}
