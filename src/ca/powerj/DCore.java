package ca.powerj;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Map.Entry;

class DCore {
	int noRows = 0;
	LBase pj;
	String dbName = "Database";
	Connection connection = null;
	Statement stm = null;
	Hashtable<Byte, PreparedStatement> pstms = null;

	DCore(LBase parent) {
		this.pj = parent;
		pstms = new Hashtable<Byte, PreparedStatement>();
	}

	void close() {
		closeStms(true);
		try {
			if (connection != null) {
				if (!connection.isClosed()) {
					connection.close();
				}
			}
		} catch (Exception ignore) {
		} finally {
			connection = null;
		}
	}

	void closeRst(ResultSet rst) {
		try {
			if (rst != null) {
				if (!rst.isClosed())
					rst.close();
			}
		} catch (Exception ignore) {
		}
	}

	void closeStm(Statement stm) {
		try {
			if (stm != null) {
				if (!stm.isClosed())
					stm.close();
			}
		} catch (Exception ignore) {
		}
	}

	void closeStm(byte id) {
		try {
			if (pstms.get(id) != null) {
				if (!pstms.get(id).isClosed())
					pstms.get(id).close();
				pstms.remove(id);
			}
		} catch (Exception ignore) {
		}
	}

	/** Closes the statements array. */
	void closeStms(boolean closeAll) {
		int after = (closeAll ? 0 : 5);
		for (Entry<Byte, PreparedStatement> entry : pstms.entrySet()) {
			if (entry.getKey() > after) {
				closeStm(entry.getValue());
			}
		}
	}

	boolean connected() {
		boolean isConnected = false;
		try {
			if (connection != null) {
				if (!connection.isClosed()) {
					if (connection.isValid(5)) {
						isConnected = true;
					}
				}
			}
		} catch (Exception ignore) {
		}
		return isConnected;
	}

	int execute(byte id) {
		noRows = 0;
		try {
			noRows = pstms.get(id).executeUpdate();
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return noRows;
	}

	int execute(String sql) {
		noRows = 0;
		try {
			noRows = stm.executeUpdate(sql);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return noRows;
	}

	byte getByte(byte id) {
		byte b = 0;
		ResultSet rst = null;
		try {
			rst = pstms.get(id).executeQuery();
			while (rst.next()) {
				b = rst.getByte(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return b;
	}

	int getInt(byte id) {
		int n = 0;
		ResultSet rst = null;
		try {
			rst = pstms.get(id).executeQuery();
			while (rst.next()) {
				n = rst.getInt(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return n;
	}

	long getLong(byte id) {
		long l = 0;
		ResultSet rst = null;
		try {
			rst = pstms.get(id).executeQuery();
			while (rst.next()) {
				if (rst.getTimestamp(1) != null) {
					l = rst.getTimestamp(1).getTime();
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return l;
	}

	ResultSet getResultSet(byte id) {
		ResultSet rst = null;
		try {
			rst = pstms.get(id).executeQuery();
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return rst;
	}

	ResultSet getResultSet(String sql) {
		ResultSet rst = null;
		try {
			rst = stm.executeQuery(sql);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return rst;
	}

	short getShort(byte id) {
		short s = 0;
		ResultSet rst = null;
		try {
			rst = pstms.get(id).executeQuery();
			while (rst.next()) {
				s = rst.getShort(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return s;
	}

	PreparedStatement getStatement(byte id) {
		return pstms.get(id);
	}

	String getString(byte id) {
		String s = "";
		ResultSet rst = null;
		try {
			rst = getResultSet(id);
			while (rst.next()) {
				s = rst.getString(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return s;
	}

	long getTime(byte id) {
		long time = 0;
		ResultSet rst = null;
		try {
			rst = getResultSet(id);
			while (rst.next()) {
				if (rst.getTimestamp(1) != null) {
					time = rst.getTimestamp(1).getTime();
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return time;
	}

	CallableStatement prepareCallables(String sql) {
		CallableStatement call = null;
		try {
			call = connection.prepareCall(sql);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return call;
	}

	PreparedStatement prepareStatement(String sql) {
		PreparedStatement pstm = null;
		try {
			pstm = connection.prepareStatement(sql);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return pstm;
	}

	void remove(byte id) {
		closeStm(pstms.get(id));
		pstms.remove(id);
	}

	void setByte(byte id, int param, byte b) {
		try {
			pstms.get(id).setByte(param, b);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setDate(byte id, int param, long time) {
		try {
			pstms.get(id).setDate(param, new java.sql.Date(time));
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setDouble(byte id, int param, double d) {
		try {
			pstms.get(id).setDouble(param, d);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setInt(byte id, int param, int i) {
		try {
			pstms.get(id).setInt(param, i);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setLong(byte id, int param, long l) {
		try {
			pstms.get(id).setLong(param, l);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setShort(byte id, int param, short s) {
		try {
			pstms.get(id).setShort(param, s);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setString(byte id, int param, String s) {
		try {
			pstms.get(id).setString(param, s);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setTime(byte id, int param, long time) {
		try {
			pstms.get(id).setTimestamp(param, new Timestamp(time));
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}
}