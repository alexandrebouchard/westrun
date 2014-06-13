package westrun;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import westrun.code.SelfBuiltRepository;
import westrun.exprepo.ExperimentsRepository;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;

import briefj.BriefIO;
import briefj.opt.InputFile;
import briefj.opt.Option;
import briefj.repo.GitRepository;
import briefj.run.Mains;
import briefj.run.Results;
import briefj.unix.RemoteUtils;



public class Launch implements Runnable
{
  @InputFile
  @Option(required = true)
  public File templateFile;

//  @Option(required = true)
//  public String remoteHost;
//  
//  @Option(required = true)
//  public String remoteExperimentDirectory;
//  
//  @Option
//  public File localCodeRepository = null;
  
  @Option
  public String remoteLaunchCommand = "qsub";

//  public static final String SYNC_BOOKMARK_NAME = "wrun-sync";

  private ExperimentsRepository repo;

  @Override
  public void run()
  {
    repo = ExperimentsRepository.fromWorkingDirectoryParents();
    
    // clone code repo
    if (!StringUtils.isEmpty(repo.codeRepository))
    {
      File repository = cloneRepository();
      if (SelfBuiltRepository.loadSpecification(repository) != null)
        SelfBuiltRepository.build(repository);
    }
    
    // prepare scripts
    List<File> launchScripts = PrepareExperiments.prepare(templateFile);
    
    // sync up
    Sync.sync();
    
    // run the commands (Later: collect the id?)
    System.out.println(launch(launchScripts));
  }
  
  public static void main(String [] args) throws InvalidRemoteException, TransportException, GitAPIException
  {
    Mains.instrumentedRun(args, new Launch());
  }

  private File cloneRepository()
  {
    File localCodeRepository = new File(repo.codeRepository);
    GitRepository gitRepo = GitRepository.fromLocal(localCodeRepository);
    String commitId = gitRepo.getCommitIdentifier(); 
    BriefIO.write(Results.getFileInResultFolder("codeCommitIdentifier"), commitId);
    
    List<File> dirtyFile = gitRepo.dirtyFiles();
    if (!dirtyFile.isEmpty())
      throw new RuntimeException("There are dirty files in the repository: " + Joiner.on("\n").join(dirtyFile));
    
    File destination = Results.getFolderInResultFolder("code");
    try 
    {
      Git.cloneRepository()
        .setURI(localCodeRepository.getAbsolutePath())
        .setDirectory(destination)
        .call();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    
    return destination;
  }

  private String launch(List<File> launchScripts)
  {
    List<String> commands = Lists.newArrayList();
    commands.add("cd " + repo.remoteDirectory);
    for (File launchScript : launchScripts)
      commands.add(remoteLaunchCommand + " " + launchScript);
    return RemoteUtils.remoteBash(repo.sshRemoteHost, commands);
  }


}
