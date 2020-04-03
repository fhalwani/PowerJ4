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

	DCore(LBase parent) {
		this.pj = parent;
	}

	void close() {
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

	void close(ResultSet rst) {
		try {
			if (rst != null) {
				if (!rst.isClosed())
					rst.close();
			}
		} catch (Exception ignore) {
		}
	}

	void close(Statement stm) {
		try {
			if (stm != null) {
				if (!stm.isClosed())
					stm.close();
			}
		} catch (Exception ignore) {
		}
	}

	void close(PreparedStatement pstm) {
		try {
			pstm.close();
		} catch (Exception ignore) {
		}
	}

	void close(Hashtable<Byte, PreparedStatement> pstms) {
		for (Entry<Byte, PreparedStatement> entry : pstms.entrySet()) {
			close(entry.getValue());
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

	int execute(PreparedStatement pstm) {
		noRows = 0;
		try {
			noRows = pstm.executeUpdate();
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

	byte getByte(PreparedStatement pstm) {
		byte b = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				b = rst.getByte(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return b;
	}

	int getInt(PreparedStatement pstm) {
		int n = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				n = rst.getInt(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return n;
	}

	long getLong(PreparedStatement pstm) {
		long l = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				l = rst.getLong(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return l;
	}

	ResultSet getResultSet(PreparedStatement pstm) {
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
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

	short getShort(PreparedStatement pstm) {
		short s = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				s = rst.getShort(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return s;
	}

	String getString(PreparedStatement pstm) {
		String s = "";
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				s = rst.getString(1);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return s;
	}

	long getTime(PreparedStatement pstm) {
		long time = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				if (rst.getTimestamp(1) != null) {
					time = rst.getTimestamp(1).getTime();
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return time;
	}

	CallableStatement prepareCallables(String sql) {
		CallableStatement cstm = null;
		try {
			cstm = connection.prepareCall(sql);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return cstm;
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

	void setByte(PreparedStatement pstm, int param, byte b) {
		try {
			pstm.setByte(param, b);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setDate(PreparedStatement pstm, int param, long time) {
		try {
			pstm.setDate(param, new java.sql.Date(time));
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setDouble(PreparedStatement pstm, int param, double d) {
		try {
			pstm.setDouble(param, d);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setInt(PreparedStatement pstm, int param, int i) {
		try {
			pstm.setInt(param, i);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setLong(PreparedStatement pstm, int param, long l) {
		try {
			pstm.setLong(param, l);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setShort(PreparedStatement pstm, int param, short s) {
		try {
			pstm.setShort(param, s);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setString(PreparedStatement pstm, int param, String s) {
		try {
			pstm.setString(param, s);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	void setTime(PreparedStatement pstm, int param, long time) {
		try {
			pstm.setTimestamp(param, new Timestamp(time));
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}
}