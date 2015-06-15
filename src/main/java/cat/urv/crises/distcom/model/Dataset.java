package cat.urv.crises.distcom.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Dataset implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Type of the dataset, which can be a set or a function. */
	public static final String TYPE = "dataset_type";
	
	/** Value for SET type. */
	public static final String TYPE_SET = "dataset_type_set";
	
	/** Value for FUNCTION type. */
	public static final String TYPE_FUNCTION = "dataset_type_function";
	
	/** Size of the dataset. Length of the VALUES list. */
	public static final String SIZE = "dataset_size";
	
	/** Data types of the dataset, which can be strings or numbers. */
	public static final String DATA_TYPES = "dataset_data_type";
	
	/** Value for NUMERIC data type. */
	public static final String DATA_TYPES_NUMERIC = "dataset_data_type_numeric";
	
	/** Value for STRINGS data type. */
	public static final String DATA_TYPES_STRINGS = "dataset_data_type_strings";
	
	/** VALUES field. */
	public static final String VALUES = "dataset_values";
	
	
	private String type;
	private int size;
	private String dataTypes;
	private ArrayList<String> values;

	public Dataset(JSONObject o) {
		this.type = (String) o.get(TYPE);
		this.size = Integer.parseInt((String) o.get(SIZE));
		this.dataTypes = (String) o.get(DATA_TYPES);
		values = new ArrayList<String>(size);
		JSONArray jsonArray = (JSONArray) o.get(VALUES);
		for (Object obj: jsonArray) {
			values.add((String) obj);
		}
	}
	
	public Dataset(String type, String dataTypes, ArrayList<String> values) {
		this.type = type;
		this.dataTypes = dataTypes;
		this.values = values;
		this.size = values.size();
	}

	public BigInteger[] encode() throws Exception {
		if (this.type.equals(TYPE_FUNCTION)) {
			if (this.dataTypes.equals(DATA_TYPES_NUMERIC)) {
				return encodeFunction(toInteger(this.values));
			} else {
				//TODO: define exception
				throw new Exception();
			}
		} else {
			return encodeSet(this.values);
		}
	}
	
	public String getType() {
		return type;
	}

	public int getSize() {
		return size;
	}

	public String getDataTypes() {
		return dataTypes;
	}

	public ArrayList<String> getValues() {
		return values;
	}
	
	private BigInteger[] encodeFunction(List<Integer> list) throws UnsupportedEncodingException {
		List<String> tmpEncoding = new ArrayList<String>();
		int pos = 1;
		for (Integer i: list) {
			for (int j = 0; j < i; j++) {
				tmpEncoding.add("{" + pos + "||" + j + "}");
			}
			pos++;
		}
		BigInteger[] encoding = new BigInteger[tmpEncoding.size()];
		for (int i = 0; i < encoding.length; i++) {
			encoding[i] = new BigInteger(1, tmpEncoding.get(i).getBytes("UTF-8"));
		}
		return encoding;
	}
	
	private BigInteger[] encodeSet(List<String> list) throws UnsupportedEncodingException {
		BigInteger[] encoding = new BigInteger[list.size()];
		for (int i = 0; i < list.size(); i++) {
			encoding[i] = new BigInteger(1, list.get(i).getBytes("UTF-8"));
		}
		return encoding;
	}
	
	private List<Integer> toInteger(List<String> list) {
		List<Integer> intList = new ArrayList<Integer>();
		for (String s : list) {
			intList.add(Integer.parseInt(s));
		}
		return intList;
	}

	/**
	 * Parses a Dataset from a JSON in a file at the provided path.
	 * @param path Path of the file.
	 * @return A Dataset object or null.
	 */
	public static Dataset loadFile(String path) {
		File f = new File(path);
		return loadFile(f);
	}
	
	/**
	 * Parses a Dataset from a JSON in a file.
	 * @param file File containing a JSON representation of the dataset.
	 * @return A Dataset object or null.
	 */
	public static Dataset loadFile(File file) {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("Dataset file not found.");
			return null;
		}
		return loadFile(reader);
	}
	
	/**
	 * Parses a Dataset from a JSON in a file.
	 * @param reader Reader handle of the file.
	 * @return A Dataset object or null.
	 */
	public static Dataset loadFile(Reader reader) {
		JSONParser parser = new JSONParser();
		JSONObject dataset;
		try {
			dataset = (JSONObject) parser.parse(reader);
		} catch (IOException e) {
			System.err.print("Error parsing dataset.");
			return null;
		} catch (ParseException e) {
			System.err.print("Error parsing dataset.");
			return null;
		}
		return new Dataset(dataset);
	}
	
	@SuppressWarnings("unchecked")
	public String toJSON() {
		JSONObject json = new JSONObject();
		json.put(DATA_TYPES, dataTypes);
		json.put(SIZE, ""+values.size());
		json.put(TYPE, type);
		json.put(VALUES, values);
		return json.toJSONString();
	}
}
