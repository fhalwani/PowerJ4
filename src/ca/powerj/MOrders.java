package ca.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class MOrders {
	private short orderID = 0, groupID = 0;
	private OOrderGroup orderGroup = new OOrderGroup();
	private HashMap<Short, Short> orders = new HashMap<Short, Short>();
	private HashMap<Short, OOrderGroup> groups = new HashMap<Short, OOrderGroup>();

	MOrders(LBase parent, PreparedStatement pstm) {
		readDB(parent, pstm);
	}

	void close() {
		orders.clear();
		groups.clear();
	}

	short getCodeID(int id) {
		switch (id) {
		case 1:
			return orderGroup.value1;
		case 2:
			return orderGroup.value2;
		case 3:
			return orderGroup.value3;
		case 4:
			return orderGroup.value4;
		default:
			return orderGroup.value5;
		}
	}

	short getGroupID() {
		return groupID;
	}

	String getGroupName() {
		return orderGroup.name;
	}

	short getOrderType() {
		return orderGroup.typID;
	}

	/** Match a master-order from PowerPath to one from Derby */
	boolean matchOrder(short id) {
		if (orderID != id) {
			groupID = orders.get(id);
			orderGroup = groups.get(groupID);
			orderID = id;
		}
		if (orderGroup != null) {
			return true;
		}
		return false;
	}

	private void readDB(LBase pj, PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		try {
			while (rst.next()) {
				if (groupID != rst.getShort("OGID")) {
					groupID = rst.getShort("OGID");
					orderGroup = groups.get(groupID);
					if (orderGroup == null) {
						orderGroup = new OOrderGroup();
						orderGroup.typID = rst.getByte("OTID");
						orderGroup.value1 = rst.getShort("OGC1");
						orderGroup.value2 = rst.getShort("OGC2");
						orderGroup.value3 = rst.getShort("OGC3");
						orderGroup.value4 = rst.getShort("OGC4");
						orderGroup.value5 = rst.getShort("OGC5");
						orderGroup.name = rst.getString("OGNM");
						groups.put(groupID, orderGroup);
					}
				}
				orders.put(rst.getShort("OMID"), groupID);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Orders Map", e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}