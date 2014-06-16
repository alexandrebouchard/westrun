package westrun.template;

import java.io.File;

import westrun.exprepo.ExperimentsRepository;



public class TemplateContext
{
  /**
   * Path to the exec folder created specially for
   * an individual point in the cross product.
   * 
   * Relative to the root of the experiments 
   * repository.
   */
  public final File individualExec;
  
  /**
   * Path to the code repository specific to this
   * run but shared by all in the cross product.
   *  
   * Relative to the root of the experiments 
   * repository.
   */
  public final File codeRepo;

  public final ExperimentsRepository expRepo;
  
  public String codeRepoName()
  {
    return expRepo.localCodeRepoRoot.getName();
  }

  public TemplateContext(File individualExec, File codeRepo, ExperimentsRepository expRepo)
  {
    this.individualExec = individualExec;
    this.codeRepo = codeRepo;
    this.expRepo = expRepo;
  }
}