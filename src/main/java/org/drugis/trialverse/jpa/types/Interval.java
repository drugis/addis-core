package org.drugis.trialverse.jpa.types;


import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.postgresql.util.PGInterval;


/**
 * Postgres Interval type
 *
 * @author bpgergo
 * @see {@link http://stackoverflow.com/questions/1945615/how-to-map-the-type-interval-in-hibernate}
 */
public class Interval implements UserType {
	private static final int[] SQL_TYPES = { Types.OTHER };

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class returnedClass() {
		return Integer.class;
	}

	@Override
	public boolean equals(final Object x, final Object y) throws HibernateException {
		return x.equals(y);
	}

	@Override
	public int hashCode(final Object x) throws HibernateException {
		return x.hashCode();
	}

	public static String getInterval(final long value){
		return new PGInterval(0, 0, 0, 0, 0, value).getValue();
	}


	@Override
	public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session, final Object owner)
			throws HibernateException, SQLException {
		final String interval = rs.getString(names[0]);
		if (rs.wasNull() || interval == null) {
			return null;
		}
		final PGInterval pgInterval = new PGInterval(interval);
		final Date epoch = new Date(0L);
		pgInterval.add(epoch);
		final Long duration = Long.valueOf(epoch.getTime());
		return new Period((long)duration).normalizedStandard();
	}

	@Override
	public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SessionImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.VARCHAR);
		} else {
			//this http://postgresql.1045698.n5.nabble.com/Inserting-Information-in-PostgreSQL-interval-td2175203.html#a2175205
			final long duration = (((Period) value).toDurationFrom(new Instant(0L))).getMillis();
			st.setObject(index, getInterval(duration), Types.OTHER);
		}
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(final Object value) throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public Object assemble(final Serializable cached, final Object owner)
			throws HibernateException {
		return cached;
	}

	@Override
	public Object replace(final Object original, final Object target, final Object owner)
			throws HibernateException {
		return original;
	}



}