package org.drugis.trialverse.model;

import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

public class ConceptTypeEnumConverter implements UserType {

    private static final int[] SQL_TYPES = new int[]{Types.OTHER};
	private static final String COLUMN = "type";

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
    throws HibernateException, SQLException {
    	Object pgObject = rs.getObject(COLUMN); // X is the column containing the enum

    	try {
    		Method valueMethod = pgObject.getClass().getMethod("getValue");
    		String value = (String)valueMethod.invoke(pgObject);			
    		return ConceptType.valueOf(value);		
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}

    	return null;
    }

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
	throws HibernateException, SQLException {
		ConceptType type = (ConceptType) value;
		st.setCharacterStream(index, new StringReader(type.name()));
	}

    public int[] sqlTypes() {		
    	return SQL_TYPES;
    }

	@Override
	public Class<ConceptType> returnedClass() {
		return ConceptType.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return false;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return 0;
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return null;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return null;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return null;
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return null;
	}

}
