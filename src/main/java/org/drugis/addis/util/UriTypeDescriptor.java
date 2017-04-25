package org.drugis.addis.util;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by joris on 19-4-17.
 */
public class UriTypeDescriptor extends AbstractTypeDescriptor<URI> {
  public static final UriTypeDescriptor INSTANCE = new UriTypeDescriptor();

  public UriTypeDescriptor() {
    super( URI.class );
  }

  public String toString(URI value) {
    return value.toString();
  }

  public URI fromString(String string) {
    try {
      return new URI(string);
    } catch (URISyntaxException e) {
      throw new HibernateException( "Unable to convert string [" + string + "] to URI : " + e );
    }
  }

  @SuppressWarnings({ "unchecked" })
  public <X> X unwrap(URI value, Class<X> type, WrapperOptions options) {
    if ( value == null ) {
      return null;
    }
    if ( String.class.isAssignableFrom( type ) ) {
      return (X) toString( value );
    }
    throw unknownUnwrap( type );
  }

  public <X> URI wrap(X value, WrapperOptions options) {
    if ( value == null ) {
      return null;
    }
    if ( String.class.isInstance( value ) ) {
      return fromString( (String) value );
    }
    throw unknownWrap( value.getClass() );
  }
}