package types;

import java.util.List;

/**
 * Defines the properties for a table.
 * <p>
 * Do not modify the protocols.
 */
@SuppressWarnings("preview")
public record Table (
	Map<String, Object> schema,
	Map<Object, List<Object>> state
) {
	@SuppressWarnings("unchecked")
	public List<String> getColumnNames(){
		return (List <String>) this.schema().get("column_names");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getColumnTypes(){
		return (List <String>) this.schema().get("column_types");
	}
	
	
	public String getTableName(){
		return (String) this.schema().get("table_name");
	}
	
	public int getPrimaryIndex(){
		return (int) this.schema().get("primary_index");
	}
	
	public String getPrimaryName() {
		return getColumnNames().get(getPrimaryIndex());
	}
	
	public int getRowCount() {
		return this.state.size();
	}
	
}
