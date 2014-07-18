package westrun;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.mvel2.templates.TemplateRuntime;

import westrun.code.SelfBuiltRepository;
import westrun.exprepo.ExpRepoPath;
import westrun.exprepo.ExperimentsRepository;
import westrun.template.CrossProductTemplate;
import westrun.template.TemplateContext;
import briefj.BriefIO;
import briefj.opt.InputFile;
import briefj.opt.Option;
import briefj.repo.GitRepository;
import briefj.run.Mains;
import briefj.run.Results;
import briefj.unix.RemoteUtils;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;



public class Launch implements Runnable
{
  @InputFile(copy = true)
  @Option(required = true)
  public File templateFile;
  
  @Option(condReq = "test=false")
  public String description;
  
  @Option(gloss = "Only runs one of the items in the cross product " +
  		"interactively, by passing qsub for testing.")
  public boolean test = false;
  
  @Option(gloss = "If we should continue the launch even if some " +
  		"files are not committed into the code repo.")
  public boolean tolerateDirtyCode = false;
  
  @Option(gloss = "Path to look for the qsub command.")
  public ArrayList<String> qsubPaths = (ArrayList<String>) Lists.newArrayList("/opt/torque/bin", "/opt/bin");
  
  private ExperimentsRepository repo;

  @Override
  public void run()
  {
    repo = ExperimentsRepository.fromWorkingDirParents();
    
    checkValidInputs();
    
    // clone code repo
    if (repo.hasCodeRepository())
    {
      // clone
      System.out.println("Cloning code");
      File repository = cloneRepository();
      
      // build
      if (SelfBuiltRepository.loadSpecification(repository) != null)
        SelfBuiltRepository.build(repository);
      
      // transfer code delta
      Sync.pushCode(repo);
      
      // next, we need to make sure that experiments will run smoothly
      // even if we have several at the same time running with different 
      // versions of code
      
      // current paths
      File local1  = repo.resolveLocal(ExpRepoPath.CODE_TO_TRANSFER); 
      File remote1 = repo.resolveRemote(ExpRepoPath.CODE_TO_TRANSFER);
      
      // pools of unique code repos
      File localCodePool = repo.resolveLocal(ExpRepoPath.CODE_TRANSFERRED);
      File remoteCodePool= repo.resolveRemote(ExpRepoPath.CODE_TRANSFERRED);
      
      // new, unique code repos
      File local2 = new File(localCodePool, uniqueCodeRepoName());
      File remote2 = new File(remoteCodePool, uniqueCodeRepoName());
      
      try { FileUtils.copyDirectory(local1, local2); }
      catch (Exception e) { throw new RuntimeException(e); }
      
      RemoteUtils.remoteBash(repo.sshRemoteHost, Arrays.asList(
          "cp -r " +  remote1 + " " + remote2));
    }
    
    // prepare scripts
    List<File> launchScripts = prepareLaunchScripts();
    
    // sync up
    Sync.sync(repo);
    
    // run the commands (Later: collect the id?)
    launch(launchScripts);
    
    // move template to previous-template folder
    if (!test && templateFile.getParentFile().getAbsoluteFile().equals(repo.resolveLocal(ExpRepoPath.TEMPLATE_DRAFTS)))
    {
      File previousTemplateDir = repo.resolveLocal(ExpRepoPath.TEMPLATE_EXECUTED); 
      File destination = new File(previousTemplateDir, uniqueCodeRepoName());
      templateFile.renameTo(destination);
      System.out.println("Executed template file moved to " + destination.getAbsolutePath());
    }
  }
  
  private void checkValidInputs()
  {
    if (!templateFile.exists() || templateFile.isDirectory())
      throw new RuntimeException("Template path not valid: " + templateFile.getAbsolutePath());
  }

  private String uniqueCodeRepoName()
  {
    return Results.getResultFolder().getName().replace(".exec", "");
  }
  
  private File codeRepo()
  {
    if (repo.hasCodeRepository())
      return new File(ExpRepoPath.CODE_TRANSFERRED.getName(), uniqueCodeRepoName());
    else
      return null;
  }
  
  public static void main(String [] args) throws InvalidRemoteException, TransportException, GitAPIException
  {
    Mains.instrumentedRun(args, new Launch());
  }

  private File cloneRepository()
  {
    File localCodeRepository = repo.localCodeRepoRoot;
    GitRepository gitRepo = GitRepository.fromLocal(localCodeRepository);
    String commitId = gitRepo.getCommitIdentifier(); 
    BriefIO.write(Results.getFileInResultFolder("codeCommitIdentifier"), commitId);
    
    List<File> dirtyFile = gitRepo.dirtyFiles();
    if (!tolerateDirtyCode && !dirtyFile.isEmpty())
      throw new RuntimeException("There are dirty files in the repository (use -tolerateDirtyCode to bypass):\n" + Joiner.on("\n").join(dirtyFile));
    
    File destination = repo.resolveLocal(ExpRepoPath.CODE_TO_TRANSFER); //new File(repo.root(), CODE_TO_TRANSFER); //Results.getFolderInResultFolder("code");
    try { FileUtils.deleteDirectory(destination); } 
    catch (IOException e) { throw new RuntimeException(e); }
    
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

  private void launch(List<File> launchScripts)
  {
    String remoteLaunchCommand = test ? "bash" : "qsub -d " + repo.remoteExpRepoRoot;
    
    List<String> commands = Lists.newArrayList();
    commands.add("PATH=$PATH:" + Joiner.on(":").join(qsubPaths));
    commands.add("cd " + repo.remoteExpRepoRoot); 
    
    for (File launchScript : launchScripts)
      commands.add(remoteLaunchCommand + " " + launchScript);
    
    if (test) 
    {
      System.out.println("Starting test. Mirrored output:");
      RemoteUtils.ssh
        .withArg(repo.sshRemoteHost).appendArgs("/bin/bash -s")
        .withStandardOutMirroring()
        .callWithInputStreamContents(Joiner.on("\n").join(commands));
    }
    else
    {
      System.out.println("Submitting " + launchScripts.size() + " qsub jobs");
      String result = RemoteUtils.remoteBash(repo.sshRemoteHost, commands);
      
      PrintWriter out = BriefIO.output(Results.getFileInResultFolder("qsubOutput.map"));
      String [] ids = result.split("\n");
      for (int i = 0; i < ids.length; i++)
        out.println("" + ids[i] + "\t" + execFolders.get(i));
      out.close();
    }
  }

  /**
   * Break the template into many templates using the @@ annotation documented
   * in class CrossProductTemplate. Put these files in the execution folder 
   * (called the shared execution folder). Then, apply MVEL to resolve the @ templates
   * in each. So far, this second phase is only used to give access to the shared exec 
   * directory via @{sharedExec}, and to individual exec folder with @{individualExec}
   * 
   * Then each template is chmoded to 777 to be executable.
   * 
   * @param templateFile
   * @return list of relative paths to the generated scripts
   */
  private List<File> prepareLaunchScripts()
  {
    String templateContents = BriefIO.fileToString(templateFile);
    List<String> expansions = CrossProductTemplate.expandTemplate(templateContents);
    File scripts = Results.getFolderInResultFolder("launchScripts");
    
    List<File> result = Lists.newArrayList();
    loop:for (int i = 0; i < expansions.size(); i++)
    {
      String expansion = expansions.get(i);
      
      // create an exec for the child
      String execFolderName = Results.nextRandomResultFolderName();
      execFolders.add(execFolderName);
      File indivExec = (new File(new File(Results.getPoolFolder(), "all"), execFolderName));
      indivExec.mkdir();
      TemplateContext context = new TemplateContext(indivExec, codeRepo(), repo);
      
      // interpret the template language
      expansion = (String) TemplateRuntime.eval(expansion, context);
      
      // write the generated file
      File generated = new File(scripts, "script-" + i + ".bash");
      BriefIO.write(generated, expansion);
      generated.setExecutable(true);
      result.add(generated);
      
      if (test)
        break loop;
    }
    return result;
  }
  
  private List<String> execFolders = Lists.newArrayList();
}
