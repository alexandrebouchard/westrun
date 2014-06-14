package westrun.template;



public class TemplateContext
{
  public final String sharedExec;
  public final String individualExec;

  TemplateContext(String sharedExec, String individualExec)
  {
    this.sharedExec = sharedExec;
    this.individualExec = individualExec;
  }
}