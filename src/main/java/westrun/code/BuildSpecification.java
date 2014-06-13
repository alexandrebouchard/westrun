package westrun.code;

import java.io.File;
import java.util.List;

import briefj.BriefIO;

import com.beust.jcommander.internal.Lists;



public class BuildSpecification
{
  public List<CommandSpecification> buildCommands = Lists.newArrayList();
  
  public void save(File destination)
  {
    String spec = BriefIO.createGson().toJson(this);
    BriefIO.write(destination, spec);
  }
}