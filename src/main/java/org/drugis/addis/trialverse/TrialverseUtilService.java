package org.drugis.addis.trialverse;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

/**
 * Created by connor on 8-4-16.
 */
public class TrialverseUtilService {
  public static final String URI = "uri";
  public static final String LITERAL = "literal";
  public static final String TYPED_LITERAL = "typed-literal";
  private static final String DOUBLE_TYPE = "http://www.w3.org/2001/XMLSchema#double";
  private static final String INTEGER_TYPE = "http://www.w3.org/2001/XMLSchema#integer";

  public static String subStringAfterLastSymbol(String inStr, char symbol) {
    return inStr.substring(inStr.lastIndexOf(symbol) + 1);
  }

  public static <T> T readValue(JSONObject row, String name) throws ReadValueException {
    String type  = JsonPath.read(row, "$." + name + ".type");
    if(LITERAL.equals(type)) {
      return JsonPath.read(row, "$." + name + ".value");
    }
    else if(URI.equals(type)){
      return JsonPath.read(row, "$."+ name + ".value");
    }
    else if(TYPED_LITERAL.equals(type)){
      String dataType  = JsonPath.read(row, "$." + name + ".datatype");
      if(dataType.equals(DOUBLE_TYPE)){
        return (T) new Double(Double.parseDouble(JsonPath.read(row, "$."+ name +".value")));
      }
      if(dataType.equals(INTEGER_TYPE)){
        return (T) new Integer(Integer.parseInt(JsonPath.read(row, "$."+ name +".value")));
      }
    }
    throw new ReadValueException("can not read value with name:" + name + " from row" + (row != null ?row.toJSONString():""));
  }
}
