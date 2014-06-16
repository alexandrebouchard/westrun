package westrun.template;

import java.io.File;



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

  public TemplateContext(File individualExec, File codeRepo)
  {
    this.individualExec = individualExec;
    this.codeRepo = codeRepo;
  }
}