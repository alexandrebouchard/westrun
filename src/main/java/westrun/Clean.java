package westrun;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.beust.jcommander.internal.Lists;

import binc.Command;
import briefj.BriefFiles;
import briefj.BriefIO;
import westrun.exprepo.ExpRepoPath;
import westrun.exprepo.ExperimentsRepository;



public class Clean
{
  public static void main(String [] args)
  {
    ExperimentsRepository repo = ExperimentsRepository.fromWorkingDirParents();
    
    // TODO: detect if remote jobs are running
    
    System.out.println("Clean should not be used if remote jobs are running.");
    System.out.println("Also avoid cleaning if you did or plan to change the code repo.");
    String answer = BriefIO.prompt("Confirm (y/n)");
    
    if (!"y".equals(answer))
      System.exit(1);
    
    Sync.pushLocalToRemote(repo);
    
    // TODO: - erase contents of transferred code dir
    //       - create a fresh one
    //       - change the links in the results to point to the new copy
    
    List<File> directoriesToDelete = Lists.newArrayList();
    for (File f : BriefFiles.ls(repo.resolveLocal(ExpRepoPath.CODE_TRANSFERRED)))
      if (f.isDirectory())
        directoriesToDelete.add(f);
    
    for (File toDel : directoriesToDelete)
    {
      // delete
      try { FileUtils.deleteDirectory(toDel); }
      catch (Exception e) { throw new RuntimeException(e); }
      
      // create a link to the latest transferred
      Command.call(Command.cmd("ln")
          .withArgs("-s")
          .appendArg("../" + ExpRepoPath.CODE_TO_TRANSFER.getPathRelativeToExpRepoRoot())
          .appendArg(toDel.toString()));
    }
    
    Sync.pushCodeTransferred(repo);
  }
}
