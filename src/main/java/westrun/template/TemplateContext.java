package westrun.template;

import java.io.File;



public class TemplateContext
{
  public final String sharedExec;
  public final String individualExec;
  public final String codeRepoName;
  
  /**
   * Root of a copy of the code repository created for this run
   */
  public String codeRepoRoot;

  TemplateContext(String sharedExec, String individualExec, String codeRepoRoot, String codeRepoName)
  {
    this.sharedExec = sharedExec;
    this.individualExec = individualExec;
    this.codeRepoRoot = codeRepoRoot;
    this.codeRepoName = codeRepoName;
  }
}