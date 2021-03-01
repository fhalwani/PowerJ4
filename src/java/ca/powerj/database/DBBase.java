package ca.powerj.database;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

class DBBase {
	int noRows = 0;
	LibBase base;
	String dbName = "Database";
	Connection connection = null;

	DBBase(LibBase base) {
		this.base = base;
	}

	public void close() {
		try {
			if (connection != null) {
				if (!connection.isClosed()) {
					connection.close();
				}
				connection = null;
			}
		} catch (SQLException ignore) {
		} finally {
			connection = null;
		}
	}

	public void close(ResultSet rst) {
		try {
			if (rst != null) {
				if (!rst.isClosed()) {
					rst.close();
				}
				rst = null;
			}
		} catch (SQLException ignore) {
		}
	}

	public void close(Statement stm) {
		try {
			if (stm != null) {
				if (!stm.isClosed()) {
					stm.close();
				}
				stm = null;
			}
		} catch (SQLException ignore) {
		}
	}

	void close(PreparedStatement pstm) {
		try {
			if (pstm != null) {
				if (!pstm.isClosed()) {
					pstm.close();
				}
				pstm = null;
			}
		} catch (Exception ignore) {
		}
	}

	public void close(HashMap<Byte, PreparedStatement> pstms) {
		if (pstms != null) {
			for (Entry<Byte, PreparedStatement> entry : pstms.entrySet()) {
				close(entry.getValue());
			}
		}
	}

	public boolean connected() {
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

	public int execute(PreparedStatement pstm) {
		noRows = 0;
		try {
			noRows = pstm.executeUpdate();
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
		return noRows;
	}

	public int execute(String sql) {
		noRows = 0;
		Statement stm = null;
		try {
			stm = connection.createStatement();
			noRows = stm.executeUpdate(sql);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(stm);
		}
		return noRows;
	}

	public byte getByte(PreparedStatement pstm) {
		byte b = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				b = rst.getByte(1);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return b;
	}

	public int getInt(PreparedStatement pstm) {
		int n = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				n = rst.getInt(1);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return n;
	}

	public long getLong(PreparedStatement pstm) {
		long l = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				l = rst.getLong(1);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return l;
	}

	public ResultSet getResultSet(PreparedStatement pstm) {
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
		return rst;
	}

	public ResultSet getResultSet(String sql) {
		Statement stm = null;
		ResultSet rst = null;
		try {
			stm = connection.createStatement();
			rst = stm.executeQuery(sql);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
			close(stm);
		}
		return rst;
	}

	public short getShort(PreparedStatement pstm) {
		short value = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				value = rst.getShort(1);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public String getString(PreparedStatement pstm) {
		String value = "";
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				value = rst.getString(1);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public long getTime(PreparedStatement pstm) {
		long value = 0;
		ResultSet rst = null;
		try {
			rst = pstm.executeQuery();
			while (rst.next()) {
				if (rst.getTimestamp(1) != null) {
					value = rst.getTimestamp(1).getTime();
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public CallableStatement prepareCallables(String sql) {
		CallableStatement cstm = null;
		try {
			cstm = connection.prepareCall(sql);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
		return cstm;
	}

	public PreparedStatement prepareStatement(String sql) {
		PreparedStatement pstm = null;
		try {
			pstm = connection.prepareStatement(sql);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
		return pstm;
	}

	public void setByte(PreparedStatement pstm, int param, byte b) {
		try {
			pstm.setByte(param, b);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	public void setDate(PreparedStatement pstm, int param, long time) {
		try {
			pstm.setDate(param, new java.sql.Date(time));
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	public void setDouble(PreparedStatement pstm, int param, double d) {
		try {
			pstm.setDouble(param, d);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	public void setInt(PreparedStatement pstm, int param, int i) {
		try {
			pstm.setInt(param, i);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	public void setLong(PreparedStatement pstm, int param, long l) {
		try {
			pstm.setLong(param, l);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	public void setShort(PreparedStatement pstm, int param, short s) {
		try {
			pstm.setShort(param, s);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	/** Prepare all statements required by a certain task/panel. */
	public void setStatements(byte id) {
	}

	public void setString(PreparedStatement pstm, int param, String s) {
		try {
			pstm.setString(param, s);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	public void setTime(PreparedStatement pstm, int param, long time) {
		try {
			pstm.setTimestamp(param, new Timestamp(time));
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}
}