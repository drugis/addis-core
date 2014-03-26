package org.drugis.addis.trialverse.repository;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

// Copied from http://anismiles.wordpress.com/2010/08/04/postgres-enum-with-hibernate/

// This implementation works only with Postgres
public class PGEnumUserType implements EnhancedUserType, ParameterizedType {
  // Enum  class under observation
  private Class<Enum> enumClass;

  public void setParameterValues(Properties parameters) {
    String enumClassName = parameters.getProperty("enumClassName");
    try {
      enumClass = (Class<Enum>) Class.forName(enumClassName);
    } catch (ClassNotFoundException cnfe) {
      throw new HibernateException("Enum class not found", cnfe);
    }
  }

  public Object assemble(Serializable cached, Object owner)
    throws HibernateException {
    return cached;
  }

  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  public Serializable disassemble(Object value) throws HibernateException {
    return (Enum) value;
  }

  public boolean equals(Object x, Object y) throws HibernateException {
    return x == y;
  }

  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  public boolean isMutable() {
    return false;
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
    Object object = rs.getObject(names[0]);
    if (rs.wasNull()) {
      return null;
    }

    // Notice how Object is mapped to PGobject. This makes this implementation Postgres specific
    if (object instanceof PGobject) {
      PGobject pg = (PGobject) object;
      return Enum.valueOf(enumClass, pg.getValue());
    }
    return null;
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
    throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, 1111);
      // UPDATE: To support NULL insertion, change to: st.setNull(index, 1111);
    } else {
      // Notice 1111 which java.sql.Type for Postgres Enum
      st.setObject(index, value, 1111);
    }
  }

  public Object replace(Object original, Object target, Object owner)
    throws HibernateException {
    return original;
  }

  public Class returnedClass() {
    return enumClass;
  }

  public int[] sqlTypes() {
    return new int[] { 1111 };
    // UPDATE: To support NULL insertion, change to: return new int[] { 1111 };
  }

  public Object fromXMLString(String xmlValue) {
    return Enum.valueOf(enumClass, xmlValue);
  }

  public String objectToSQLString(Object value) {
    return '\'' + ((Enum) value).name() + '\'';
  }

  public String toXMLString(Object value) {
    return ((Enum) value).name();
  }
}