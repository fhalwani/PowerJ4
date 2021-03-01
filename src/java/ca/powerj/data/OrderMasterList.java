package ca.powerj.data;
import java.util.ArrayList;
import java.util.HashMap;
import ca.powerj.database.DBPowerj;

public class OrderMasterList {
	private short orderID = 0, groupID = 0;
	private HashMap<Short, OrderGroupData> groups = new HashMap<Short, OrderGroupData>();
	private HashMap<Short, Short> orders = new HashMap<Short, Short>();
	private OrderGroupData orderGroup = new OrderGroupData();

	public OrderMasterList(DBPowerj dbPowerJ) {
		getData(dbPowerJ);
	}

	public void close() {
		orders.clear();
		groups.clear();
	}

	public short getCodeID(int id) {
		switch (id) {
		case 1:
			return orderGroup.getValue1();
		case 2:
			return orderGroup.getValue2();
		case 3:
			return orderGroup.getValue3();
		case 4:
			return orderGroup.getValue4();
		default:
			return orderGroup.getValue5();
		}
	}

	private void getData(DBPowerj dbPowerJ) {
		ArrayList<OrderGroupData> tempGroup = dbPowerJ.getOrderGroups();
		for (int i = 0; i < tempGroup.size(); i++) {
			groups.put(tempGroup.get(i).getGrpID(), tempGroup.get(i));
		}
		tempGroup.clear();
		ArrayList<OrderMasterData> tempMaster = dbPowerJ.getOrderMasters();
		for (int i = 0; i < tempMaster.size(); i++) {
			orders.put(tempMaster.get(i).getOrdID(), tempMaster.get(i).getGrpID());
		}
		tempMaster.clear();
	}

	public short getGroupID() {
		return groupID;
	}

	public String getGroupName() {
		return orderGroup.getName();
	}

	public short getOrderType() {
		return orderGroup.getTypeID();
	}

	/** Match a master-order from PowerPath to one from Derby */
	public boolean matchOrder(short id) {
		if (orderID != id) {
			if (orders.get(id) == null) {
				groupID = 0;
			} else {
				groupID = orders.get(id);
			}
			orderGroup = groups.get(groupID);
			orderID = id;
		}
		if (orderGroup != null) {
			return true;
		}
		return false;
	}
}