package org.drugis.addis.util;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

import java.net.URI;

/**
 * Created by joris on 19-4-17.
 */
public class UriUserType extends AbstractSingleColumnStandardBasicType<URI> implements DiscriminatorType<URI>{
  public static final UriUserType INSTANCE = new UriUserType();

  public UriUserType() {
    super( VarcharTypeDescriptor.INSTANCE, UriTypeDescriptor.INSTANCE );
  }
  public String getName() {
    return "uri";
  }
  @Override
  protected boolean registerUnderJavaType() {
    return true;
  }

  @Override
  public String toString(URI value) {
    return UriTypeDescriptor.INSTANCE.toString( value );
  }

  public String objectToSQLString(URI value, Dialect dialect) throws Exception {
    return StringType.INSTANCE.objectToSQLString( toString( value ), dialect );
  }

  public URI stringToObject(String xml) throws Exception {
    return UriTypeDescriptor.INSTANCE.fromString( xml );
  }

}
