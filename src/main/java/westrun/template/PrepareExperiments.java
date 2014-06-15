package westrun.template;

import java.io.File;
import java.util.List;

import org.mvel2.templates.TemplateRuntime;

import com.beust.jcommander.internal.Lists;

import static binc.Command.call;
import briefj.BriefIO;
import static briefj.run.Commands.chmod;
import briefj.run.Results;



public class PrepareExperiments 
{
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
  public static List<File> prepare(File templateFile, String codeRepoRoot, String projectName)
  {
    String templateContents = BriefIO.fileToString(templateFile);
    List<String> expansions = CrossProductTemplate.expandTemplate(templateContents);
    File scripts = Results.getFolderInResultFolder("launchScripts");
    
    List<File> result = Lists.newArrayList();
    for (int i = 0; i < expansions.size(); i++)
    {
      String expansion = expansions.get(i);
      
      // create an exec for the child
      String execFolderName = Results.nextRandomResultFolderName();
      (new File(Results.getPoolFolder(), "all/" + execFolderName)).mkdir();
      TemplateContext context = new TemplateContext(Results.getResultFolder().getName(), execFolderName, codeRepoRoot, projectName);
      
      // interpret the template language
      expansion = (String) TemplateRuntime.eval(expansion, context);
      
      // write the generated file
      File generated = new File(scripts, "script-" + i + ".bash");
      BriefIO.write(generated, expansion);
      call(chmod.ranIn(scripts).withArgs("777 " + generated.getName()).throwOnNonZeroReturnCode());
      result.add(generated);
    }
    return result;
  }

}
