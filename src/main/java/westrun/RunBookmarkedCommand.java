package westrun;

import java.io.File;

import static binc.Commands.bash;
import static binc.Command.call;

/**
 * This looks in parent directories to find .bm folder. Each file in them is considered
 * as a bookmark for a script, where the name of the file is the name of the bookmark,
 * and the contents is the file to be interpreted by bash.
 * 
 * The script is ran as if it were called from .bm/../
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class RunBookmarkedCommand
{

  /**
   * @param args One argument: the bookmark name
   */
  public static void main(String[] args)
  {
    if (args.length != 1)
    {
      System.err.println("The run bookmark command takes 1 argument: the name of the bookmark.");
      System.err.println("It then looks into parent directories for an invisible folder .bm/[bookmark name]");
      System.err.println("The first such file is then executed by bash.");
      System.exit(1);
    }
    run(args[0]);
  }
  
  public static void run(String bookmarkName)
  {
    run(bookmarkName, pwd());
  }
  
  public static void run(String bookmarkName, File currentDir)
  {
    // look in parent directories until a bookmark folder is found
    File bookmark = getBookmarkFile(currentDir, bookmarkName);
    if (bookmark == null)
    {
      System.err.println("No bookmark matching " + bookmarkName);
      System.exit(1);
    }
    // run the bookmark
    File execDir = bookmark.getParentFile();
    call(bash
          .withStandardOutMirroring()
          .ranIn(execDir)
          .withArg(bookmark.getAbsolutePath())
          .throwOnNonZeroReturnCode());
  }
  
  private static File pwd()
  {
    return new File("");
  }

  public static File getBookmarkFile(File f, String bookmarkName)
  {
    f = f.getAbsoluteFile();
    File potential = new File(new File(f, BOOKMARK_FOLDER_NAME), bookmarkName);
    if (potential.exists())
      return potential;
    File parent = f.getParentFile();
    if (parent == null)
      return null;
    else
      return getBookmarkFile(parent, bookmarkName);
  }
  
  public static final String BOOKMARK_FOLDER_NAME = ".bm";
}
