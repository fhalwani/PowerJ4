package ca.powerj;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

class LDefaults {
	private boolean altered = false;
	private String fileName = "";
	private HashMap<String, Object> defaults = new HashMap<String, Object>();
	
	LDefaults(LBase parent) {
		fileName = parent.appDir + System.getProperty("file.separator") +
				"bin" + System.getProperty("file.separator") + "defaults.ini";
		readFile();
	}

	void close() {
		if (altered) {
			saveFile();
		}
		defaults.clear();
	}

	int getInt(String key, int def) {
		Integer i = (Integer) defaults.get(key);
		if (i == null) {
			defaults.put(key, def);
			altered = true;
			return def;
		}
		return i.intValue();
	}

	String getString(String key, String def) {
		String s = (String) defaults.get(key);
		if (s == null) {
			defaults.put(key, def);
			altered = true;
			return def;
		}
		return s;
	}

	private void readFile() {
		Object value;
		String escaped;
		try {
			File f = new File(fileName);
			if (!f.exists()) {
				return;
			}
			Scanner scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				String[] columns = scanner.nextLine().split("=");
				// Backslash in Microsoft filename needs formatting
				escaped = columns[1].trim().replace("\\\\", "\\");
				value = toObject(escaped);
				defaults.put(columns[0].trim(), value);
			}
			scanner.close();
		} catch (NullPointerException ignore) {
		} catch (FileNotFoundException ignore) {
		}
	}

	/** Saves the tblDefaults to disk. */
	void saveFile() {
		try {
			if (altered) {
				String escaped = "";
				OEntry elem = new OEntry();
				ArrayList<OEntry> elements = new ArrayList<OEntry>();
				for (Entry<String, Object> element : defaults.entrySet()) {
					elem = new OEntry();
					elem.key = (String) element.getKey();
					elem.value = element.getValue().toString();
					elements.add(elem);
				}
				Collections.sort(elements, new Comparator<OEntry>() {
					@Override
					public int compare(OEntry o1, OEntry o2) {
						return o1.key.compareToIgnoreCase(o2.key);
					}
				});
				File f = new File(fileName);
				if (!f.exists())
					f.createNewFile();
				if (f.exists()) {
					FileOutputStream fos = new FileOutputStream(f);
					for (int i = 0; i < elements.size(); i++) {
						elem = elements.get(i);
						// Backslash in Microsoft filename needs formatting
						escaped = elem.value.replace("\\", "\\\\");
						fos.write((elem.key + "=" + escaped + System.lineSeparator()).getBytes());
					}
					fos.close();
					altered = false;
				}
			}
		} catch (IOException ignore) {
		}
	}

	void setInt(String key, int value) {
		if (value != getInt(key, value)) {
			defaults.put(key, value);
			altered = true;
		}
	}

	void setString(String key, String value) {
		if (!value.equals(getString(key, value))) {
			defaults.put(key, value);
			altered = true;
		}
	}

	private Object toObject(String s) {
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {}
		return s;
	}

	private class OEntry {
		String key = "";
		String value = "";
	}
}