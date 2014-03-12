package org.drugis.addis.analyses;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by connor on 3/11/14.
 */
public class AnalysisTypeUserType implements UserType {
  @Override
  public int[] sqlTypes() {
    return new int[]{Types.VARCHAR};
  }

  @Override
  public Class returnedClass() {
    return AnalysisType.class;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return x == null ? x == y : x.equals(y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return x == null ? 0 : x.hashCode();
  }

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
    String name = resultSet.getString(names[0]);
    try {
      return resultSet.wasNull() ? null : AnalysisType.getByLabel(name);
    } catch (Exception e) {
      throw new HibernateException(e.getMessage(), e);
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      statement.setNull(index, Types.VARCHAR);
    } else {
      statement.setString(index, value.toString());
    }
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) value;
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return cached;
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }
}
