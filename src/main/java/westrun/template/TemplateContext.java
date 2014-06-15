package westrun.template;



public class TemplateContext
{
  public final String sharedExec;
  public final String individualExec;
  public final String projectName;
  
  /**
   * Root of a copy of the code repository created for this run
   */
  public String codeRepoRoot;

  TemplateContext(String sharedExec, String individualExec, String codeRepoRoot, String projectName)
  {
    this.sharedExec = sharedExec;
    this.individualExec = individualExec;
    this.codeRepoRoot = codeRepoRoot;
    this.projectName = projectName;
  }
}