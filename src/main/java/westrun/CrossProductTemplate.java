package westrun;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import briefj.BriefStrings;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;


/**
 * Small language to expand a template string into many strings.
 * 
 * For example, the template: "I have @@{two, three, four} @@{space ships, dinos}" 
 * will be expanded into the six strings:
 * I have two space ships
 * I have three space ships
 * I have four space ships
 * I have two dinos
 * I have three dinos
 * I have four dinos
 * 
 * The syntax for the variables @@{..} supports ranges and exponential ranges as well.
 * 
 * For example, the template: "I have @@{zero, 2--3, 10^[-2 -- -1]} dinos"
 * will be expanded into the five strings:
 * I have zero dinos
 * I have 2 dinos
 * I have 3 dinos
 * I have 0.01 dinos
 * I have 0.1 dinos
 * 
 * Note that the ranges are integer valued, separated by two dashes and that the bounds are inclusive.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CrossProductTemplate
{
  public static List<String> expandTemplate(String template)
  {
    CrossProductTemplate templateInterpreter = new CrossProductTemplate();
    templateInterpreter.init(template);
    return templateInterpreter.expand();
  }
  
  /**
   * Matches @@{...}
   */
  private static final Pattern VARIABLE_REGEX = Pattern.compile("@@\\{([^\\}]*)\\}");
  
  /**
   * Matches java integers.
   */
  private static final String INTEGER_REGEX = "(?:[\\+-]?\\d+)(?:[eE][\\+-]?\\d+)?";
  
  /**
   * Matches java doubles.
   */
  private static final String DOUBLE_REGEX = "[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?";
  
  /**
   * Matches INT -- INT with some flexibility on spacing.
   */
  private static final String RANGE_REGEX_STR = "\\s*(" + INTEGER_REGEX + ")" + "\\s*--\\s*" + "(" + INTEGER_REGEX + ")\\s*";
  private static final Pattern RANGE_REGEX = Pattern.compile(RANGE_REGEX_STR);
  
  /**
   * Matches DOUBLE ^ [INT -- INT]
   */
  private static final Pattern POWER_RANGE_REGEX = Pattern.compile("\\s*(" + DOUBLE_REGEX + ")\\s*\\^\\s*\\[" + RANGE_REGEX_STR + "\\]\\s*");
  
  private List<String> literalRegions;
  private List<String> variableSpecifications;
  
  private CrossProductTemplate() {}
  
  /**
   * Parse the spec of variables (@{..} blocks) and literals (anything between variables)
   * @param template
   */
  private void init(String template)
  {
    // parse
    literalRegions = Splitter.on(VARIABLE_REGEX).splitToList(template);
    variableSpecifications = BriefStrings.firstGroupFromAllMatches(VARIABLE_REGEX, template);
    if (literalRegions.size() != variableSpecifications.size() + 1)
      throw new RuntimeException();
  }
  
  /**
   * Expand the cross-product of all the values of the variables.
   * @return
   */
  private List<String> expand()
  {
    List<List<String>> partialExpansion = Lists.newArrayList();
    partialExpansion.add(Collections.singletonList(literalRegions.get(0)));
    for (int variableIndex = 0; variableIndex < variableSpecifications.size(); variableIndex++)
      partialExpansion = variableExpansion(variableIndex, partialExpansion);
    List<String> result = Lists.newArrayList();
    for (List<String> list : partialExpansion)
      result.add(Joiner.on("").join(list));
    return result;
  }
  
  /**
   * Loop over the values of a single variable and concatenate these to the 
   * values in the expansion set in all possible ways. 
   * @param variableIndex
   * @param expansionSet
   * @return
   */
  private List<List<String>> variableExpansion(int variableIndex, List<List<String>> expansionSet)
  {
    List<List<String>> newExpansionSet = Lists.newArrayList();
    String literalSuffix = literalRegions.get(variableIndex + 1);
    for (String variableExpansion : singleVariableValues(variableSpecifications.get(variableIndex)))
      for (List<String> oldExpansion : expansionSet)
      {
        List<String> newList = Lists.newArrayList(oldExpansion);
        newList.add(variableExpansion);
        newList.add(literalSuffix);
        newExpansionSet.add(newList);
      }
    return newExpansionSet;
  }
  
  /**
   * syntax: 1, blah blah, 5--7, 10, 6 ---> [1,  blah blah,  5,  6,  7,  10,  6]
   * also: 666, 2^[2--3] ---> [666,  2,  4]
   * @param variableSpec
   * @return
   */
  private static List<String> singleVariableValues(String variableSpec)
  {
    List<String> splitByComma = Splitter.onPattern(",\\s*").splitToList(variableSpec);
    List<String> result = Lists.newArrayList();
    for (String item : splitByComma)
    {
      if (POWER_RANGE_REGEX.matcher(item).matches())
        result.addAll(powerRangeValues(item));
      else if (RANGE_REGEX.matcher(item).matches())
        result.addAll(rangeValues(item));
      else
        result.add(item);
    }
    return result;
  }
  
  private static List<String> rangeValues(String item)
  {
    final List<String> parameters = BriefStrings.allGroupsFromFirstMatch(RANGE_REGEX, item);
    final List<String> result = Lists.newArrayList();
    for (int integer : rangeValues(parameters.get(0), parameters.get(1)))
      result.add("" + integer);
    return result;
  }
  
  private static List<Integer> rangeValues(String minStr, String maxStr)
  {
    final int 
      minInclusive = Integer.parseInt(minStr),
      maxInclusive = Integer.parseInt(maxStr);
    if (minInclusive > maxInclusive)
      throw new RuntimeException("" + minInclusive + " > " + maxInclusive);
    final List<Integer> result = Lists.newArrayList();
    for (int i = minInclusive; i <= maxInclusive; i++)
      result.add(i);
    return result;
  }

  private static List<String> powerRangeValues(String item)
  {
    final List<String> parameters = BriefStrings.allGroupsFromFirstMatch(POWER_RANGE_REGEX, item);
    final double base = Double.parseDouble(parameters.get(0));
    final List<String> result = Lists.newArrayList();
    for (int integer : rangeValues(parameters.get(1), parameters.get(2)))
      result.add("" + Math.pow(base, integer));
    return result;
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    test("I have @@{zero, 2--3, 10^[-2 -- -1]} @@{dinos, space crafts}@@{!,}");
  }

  private static void test(String string)
  {
    System.out.println("Template: " + string);
    System.out.println("Expansions:");
    for (String exp : expandTemplate(string))
      System.out.println("\t" + exp);
  }

}
