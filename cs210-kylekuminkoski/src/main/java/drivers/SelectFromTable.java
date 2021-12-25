package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import maps.SearchMap;
import types.Driver;
import types.Entry;
import types.Response;
import types.Status;
import types.Table;


public class SelectFromTable implements Driver {
	//Pattern for the driver
	static final Pattern pattern = Pattern.compile(
		//SELECT\s+(\*|(?:(?:\w+)(?:\s+AS\s*[a-z]+[a-z0-9]*(?:_+\w*)?)?)(?:\s*,\s*(?:\w+)(?:\s+AS\s*[a-z]+[a-z0-9]*(?:_+\w*)?)?)*)\s*FROM\s*([a-z]+[a-z0-9]*(?:_+\w*)?)\s*(?:(WHERE)\s*([a-z]+[a-z0-9]*(?:_+\w*)?)\s*(\=|\<\>|\<|\>|\<\=|\>\=)\s*((?:\s*(?:(?:\")?\s*[a-z0-9_ +!?\\]+\s*(?:\")?)(?:(?:\s*,\s*(?:(?:\")?\s*[a-z0-9_+!?\\]+\s*(?:\")?))*)\s*)))?
		"Select\\s+(\\*|(?:(?:(?:(?:\\s*(?:\\w+(?:\\s+As\\s+\\w+)?)(?:(?:\\s*,\\s*(?:\\w+(?:\\s+As\\s+\\w+)?))*))?))))\\s+from\\s+([a-z]+[a-z0-9]*(?:_+\\w*)?)(?:\\s+(Where)\\s+(\\w+)\\s*(=|<>|<=|>=|<|>)\\s*(.+))?",
		Pattern.CASE_INSENSITIVE
	);
	
	//Pattern for Strings
	static final Pattern S = Pattern.compile(
			//\"\s*?[a-z0-9_+!? 	\\]+\s*\"
			"\\\"\\s*?[a-z0-9_+!? 	\\\\]{0,127}\\s*\\\"",
			Pattern.CASE_INSENSITIVE
		);
	
	//Pattern for Integer
	static final Pattern I = Pattern.compile(
			//((?:\+|\-)?[1-9]+[0-9]*|0)
			"((?:\\+|\\-)?[1-9]+[0-9]*|0)",
			Pattern.CASE_INSENSITIVE
		);
	
	//Pattern for Boolean
	static final Pattern B = Pattern.compile(
			//(True|False|Null)
			"(True|False|Null)",
			Pattern.CASE_INSENSITIVE
		);
	
	//Pattern for Null
	static final Pattern N = Pattern.compile(
			//Null
			"Null",
			Pattern.CASE_INSENSITIVE
		);
	
	
	
	public Response execute(String query, Database db) {
		
		Matcher matcher = pattern.matcher(query.strip());
		
		if (!matcher.matches()) 
			return new Response(query,Status.UNRECOGNIZED,null,null);
		
		String table_name = matcher.group(2).strip();
		
		if(!db.tables().contains(table_name)) {
			return new Response(query,Status.FAILED,"This table does not exist.",null);
		}
		
		String tableNameDb = (String) db.tables().get(table_name).schema().get("table_name");
		List<String> columnNamesDb = (List<String>) db.tables().get(table_name).schema().get("column_names");
		List<String> columnTypesDb = (List<String>) db.tables().get(table_name).schema().get("column_types");
		int primaryIndex= (int) db.tables().get(table_name).schema().get("primary_index");
		
		
		
		String columnList[] = matcher.group(1).strip().split(",");
		
		
		List<Integer> columnPointers = new LinkedList<>();
		List<String> resultTableNames = new LinkedList<>();
		List<String> resultTableTypes = new LinkedList<>();
		int resultTablePrimary = -1;
		
		
		if(columnList[0].equalsIgnoreCase("*")) {
			
			for(int i=0; i<db.tables().get(tableNameDb).schema().size()-1; i++) {
				
				columnPointers.add(i);	
				
			}
			resultTableNames = columnNamesDb;
			resultTableTypes = columnTypesDb;
			resultTablePrimary = primaryIndex;
		}else {
		
	
		for(String s: columnList) {
			String nameCheck[]= s.strip().split("\\s+");
			
			for(String a: resultTableNames) {
				if(nameCheck[nameCheck.length-1].equals(a)) {
					return new Response(query,Status.FAILED,"You have duplicate column names.",null);
				}
			}
			
			resultTableNames.add(nameCheck[nameCheck.length-1]);
			

			for(String o: columnNamesDb) {
				if(nameCheck[0].equals(o)) {
					
					columnPointers.add(columnNamesDb.indexOf(o));
					resultTableTypes.add(columnTypesDb.get(columnNamesDb.indexOf(o)));
					
				}
			}
			

		}
		for(int i : columnPointers) {
			if(i == primaryIndex) {
				resultTablePrimary = columnPointers.indexOf(i);
				break;
			}
			
		}
		
		}
		
		if(!columnPointers.contains(primaryIndex)) {
			return new Response(query,Status.FAILED,"There is no primary column.",null);
		}
		
		
		
		
		Table result_table = new Table(
				SearchMap.of(
					"table_name", "_select",
					"column_names", resultTableNames,
					"column_types", resultTableTypes,
					"primary_index", resultTablePrimary
				),
				new SearchMap<>()
			);
		
		
		
		
		
		if(matcher.group(3) == null && columnList[0].equalsIgnoreCase("*")) { 
			for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
				result_table.state().put(entry.key(), entry.value());
			}
			
		
		}else if(matcher.group(3) != null && columnList[0].equalsIgnoreCase("*")) {
			
			String leftHandSide = matcher.group(4).strip();
			String operator = matcher.group(5).strip();
			String rightHandSide = matcher.group(6).strip();
			
			if(!columnNamesDb.contains(leftHandSide)) 
				return new Response(query, Status.FAILED, "The column for the where condition does not exist", null);
			
			int leftPointer = columnNamesDb.indexOf(leftHandSide);
			
			String leftType = columnTypesDb.get(leftPointer);
			
			Matcher strings = S.matcher(rightHandSide);
			Matcher integers = I.matcher(rightHandSide);
			Matcher bools = B.matcher(rightHandSide);
			Matcher nulls = N.matcher(rightHandSide);
			
			if(nulls.matches()) {
				if((operator.equalsIgnoreCase("<")|| operator.equalsIgnoreCase("<=")|| operator.equalsIgnoreCase(">=")|| operator.equalsIgnoreCase(">")) && leftType.equalsIgnoreCase("boolean")) {
					return new Response(query, Status.FAILED, "Booleans cannot use this operator", null);
			
		}
				
				for(Entry<Object,List<Object>> entry: db.tables().get(tableNameDb).state()) {
					if(operator.equalsIgnoreCase("<>")) {
							result_table.state().put(entry.key(), entry.value());
					}
					
				}
			
			}else if(strings.matches()) {
				if(!leftType.equalsIgnoreCase("string")) {
					return new Response(query, Status.FAILED, "Type mismatch in where condition.", null);
				}
				
				String newRightSide = rightHandSide.substring(1,rightHandSide.length()-1);
				
				if(operator.equalsIgnoreCase("=")) {
					for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
						if(newRightSide.equals((entry.value().get(leftPointer))))
							result_table.state().put(entry.key(), entry.value());
					}
				}
				else if(operator.equalsIgnoreCase("<>")){
					for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
						if(!newRightSide.equals((entry.value().get(leftPointer))))
							result_table.state().put(entry.key(), entry.value());
					}
				}
				else if(operator.equalsIgnoreCase("<")){
					for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
						int compare = newRightSide.compareTo(((String)entry.value().get(leftPointer)));
						if(compare>0)
							result_table.state().put(entry.key(), entry.value());
					}
				}
				else if(operator.equalsIgnoreCase(">")){
					for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
						int compare = newRightSide.compareTo(((String)entry.value().get(leftPointer)));
						if(compare<0)
							result_table.state().put(entry.key(), entry.value());
					}
				}
				else if(operator.equalsIgnoreCase("<=")){
					for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
						int compare = newRightSide.compareTo(((String)entry.value().get(leftPointer)));
						if(compare>=0)
							result_table.state().put(entry.key(), entry.value());
					}
				}
				else if(operator.equalsIgnoreCase(">=")){
					for(Entry<Object, List<Object>> entry : db.tables().get(tableNameDb).state()) {
						int compare = newRightSide.compareTo(((String)entry.value().get(leftPointer)));
						if(compare<=0)
							result_table.state().put(entry.key(), entry.value());
					}
				}
				
				
			}else if(integers.matches()) {
				if(!leftType.equalsIgnoreCase("integer")) {
					return new Response(query,Status.FAILED,"Type mismatch in where condition.",null);
				}
				try {
					//make the value an integer
					int newRightHand = Integer.parseInt(rightHandSide.strip());
					
					if(operator.equalsIgnoreCase("=")) {
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							if(t.value().get(leftPointer)!=null) {
							if(newRightHand == ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
							}
						}
					}
					else if(operator.equalsIgnoreCase("<>")){
					    for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
					    	if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand != ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
					    	}
					    	else
					    		result_table.state().put(t.key(), t.value());
						}
					}
					else if(operator.equalsIgnoreCase("<")){
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							if(t.value().get(leftPointer)!=null) {
							if(newRightHand > ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
							}
						}
					}
					else if(operator.equalsIgnoreCase(">")){
					    for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
					    	if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand < ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
					    	}
						}
					}
					else if(operator.equalsIgnoreCase("<=")){
					    for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
					    	if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand >= ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
					    	}
						}
					}
					else if(operator.equalsIgnoreCase(">=")){
					    for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
					    	if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand <= ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
					    	}
						}
					}
				} //try catch used to prevent integer bounds exceptions
				catch (NumberFormatException e) {
						  return new Response(query,Status.FAILED,"Exceeded integer limit",null);
				}
			}
			//check to see if its a boolean
			else if(bools.matches()) {
				if(!leftType.equalsIgnoreCase("boolean")) {
					return new Response(query,Status.FAILED,"Type mismatch on where condition.",null);
				}
				
				if(operator.equalsIgnoreCase("<")|| operator.equalsIgnoreCase("<=")|| operator.equalsIgnoreCase(">=")|| operator.equalsIgnoreCase(">")){
					return new Response(query,Status.FAILED,"Cannot compare Booleans with this operator",null);
				}
				//make entered value into boolean
				Boolean transferBoo = Boolean.parseBoolean(rightHandSide.strip());
				if(operator.equalsIgnoreCase("=")) {
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						if(transferBoo.equals(t.value().get(leftPointer)))
						result_table.state().put(t.key(), t.value());
					}
				}
			    else if(operator.equalsIgnoreCase("<>")){
			    	for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						if(!transferBoo.equals(t.value().get(leftPointer)))
						result_table.state().put(t.key(), t.value());
					}
				}
			}
			else {
				return new Response(query,Status.FAILED,"The right hand argument could not be defined",null);
			}	
			
		}else if(matcher.group(3) != null) {
			
			String leftHandSide = matcher.group(4).strip();
			String operator = matcher.group(5).strip();
			String rightHandSide = matcher.group(6).strip();
			
			if(!columnNamesDb.contains(leftHandSide)) 
				return new Response(query, Status.FAILED, "The column for the where condition does not exist", null);
			
			int leftPointer = columnNamesDb.indexOf(leftHandSide);
			
			String leftType = columnTypesDb.get(leftPointer);
			
			Matcher strings = S.matcher(rightHandSide);
			Matcher integers = I.matcher(rightHandSide);
			Matcher bools = B.matcher(rightHandSide);
			Matcher nulls = N.matcher(rightHandSide);
			
			if(nulls.matches()) {
				if((operator.equalsIgnoreCase("<")|| operator.equalsIgnoreCase("<=")|| operator.equalsIgnoreCase(">=")|| operator.equalsIgnoreCase(">")) && leftType.equalsIgnoreCase("boolean")) {
					return new Response(query, Status.FAILED, "Booleans cannot use this operator", null);
			
		}
				
				for(Entry<Object,List<Object>> entry: db.tables().get(tableNameDb).state()) {
					LinkedList<Object> row = new LinkedList<>();
					
					for(int i=0; i< columnPointers.size(); i++) {
						//add the state value at the current index for pointer value 
						row.add(entry.value().get(columnPointers.get(i)));
					
					if(operator.equalsIgnoreCase("<>")) {
							result_table.state().put(entry.key(), row);
					}
					
				}
			
			}
			
			}else if(strings.matches()) {
				if(!leftType.equalsIgnoreCase("string")) {
					return new Response(query,Status.FAILED,"Type Mismatch on where condition",null);
				}
				//get rid of quotation marks
				String newRightHand = rightHandSide.strip().substring(1,rightHandSide.strip().length()-1);
				
				if(operator.equalsIgnoreCase("=")) {
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						if(newRightHand.equals((t.value().get(leftPointer))))
							result_table.state().put(t.key(), row);
					}
				}
				else if(operator.equalsIgnoreCase("<>")){
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						if(!(newRightHand.equals((t.value().get(leftPointer))))) {
							result_table.state().put(t.key(), row);
						}
					}
				}
				else if(operator.equalsIgnoreCase("<")){
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						int compare = newRightHand.compareTo(((String)t.value().get(leftPointer)));
						if(compare>0)
						result_table.state().put(t.key(), row);
					}
				}
				else if(operator.equalsIgnoreCase(">")){
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						int compare = newRightHand.compareTo(((String)t.value().get(leftPointer)));
						if(compare<0)
						result_table.state().put(t.key(), row);
					}
				}
				else if(operator.equalsIgnoreCase("<=")){
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						int compare = newRightHand.compareTo(((String)t.value().get(leftPointer)));
						if(compare>=0)
						result_table.state().put(t.key(), row);
					}
				}
				else if(operator.equalsIgnoreCase(">=")){
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						int compare = newRightHand.compareTo(((String)t.value().get(leftPointer)));
						if(compare<=0)
						result_table.state().put(t.key(), row);
					}
				}
			}
			//check to see if its a int
			else if(integers.matches()) {
				if(!leftType.equalsIgnoreCase("integer")) {
					return new Response(query,Status.FAILED,"Type Mismatch on the where clause",null);
				}
				try {
					//make the value an integer
					int newRightHand = Integer.parseInt(rightHandSide.strip());
					
					if(operator.equalsIgnoreCase("=")) {
						//for each row in the state
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							LinkedList<Object> row = new LinkedList<>();
							//for each pointer
							for(int i=0; i< columnPointers.size(); i++) {
								//add the state value at the current index for pointer value 
								row.add(t.value().get(columnPointers.get(i)));
							}
							//fill return table with row
							if(t.value().get(leftPointer)!=null) {
							if(newRightHand == ((int)t.value().get(leftPointer)))
							result_table.state().put(t.key(), row);
							}
						}
					}
					else if(operator.equalsIgnoreCase("<>")){
						//for each row in the state
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							LinkedList<Object> row = new LinkedList<>();
							//for each pointer
							for(int i=0; i< columnPointers.size(); i++) {
								//add the state value at the current index for pointer value 
								row.add(t.value().get(columnPointers.get(i)));
							}
							//fill return table with row
							if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand != ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), row);
					    	}
					    	else
					    		result_table.state().put(t.key(), row);
						}
					}
					else if(operator.equalsIgnoreCase("<")){
						//for each row in the state
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							LinkedList<Object> row = new LinkedList<>();
							//for each pointer
							for(int i=0; i< columnPointers.size(); i++) {
								//add the state value at the current index for pointer value 
								row.add(t.value().get(columnPointers.get(i)));
							}
							//fill return table with row
							if(t.value().get(leftPointer)!=null) {
							if(newRightHand > ((int)t.value().get(leftPointer)))
							result_table.state().put(t.key(), row);
							}
						}
					}
					else if(operator.equalsIgnoreCase(">")){
						//for each row in the state
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							LinkedList<Object> row = new LinkedList<>();
							//for each pointer
							for(int i=0; i< columnPointers.size(); i++) {
								//add the state value at the current index for pointer value 
								row.add(t.value().get(columnPointers.get(i)));
							}
							//fill return table with row
							if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand < ((int)t.value().get(leftPointer)))
							result_table.state().put(t.key(), row);
							}
						}
					}
					else if(operator.equalsIgnoreCase("<=")){
					    for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
					    	if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand >= ((int)t.value().get(leftPointer)))
								result_table.state().put(t.key(), t.value());
					    	}
						}
					}
					else if(operator.equalsIgnoreCase(">=")){
						//for each row in the state
						for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
							LinkedList<Object> row = new LinkedList<>();
							//for each pointer
							for(int i=0; i< columnPointers.size(); i++) {
								//add the state value at the current index for pointer value 
								row.add(t.value().get(columnPointers.get(i)));
							}
							//fill return table with row
							if(t.value().get(leftPointer)!=null) {
					    	if(newRightHand <= ((int)t.value().get(leftPointer)))
							result_table.state().put(t.key(), row);
							}
						}
					}
				} //try catch used to prevent integer bounds exceptions
				catch (NumberFormatException e) {
						  return new Response(query,Status.FAILED,"Exceeded integer limit",null);
				}
			}
		
			//check to see if its a boolean
			else if(bools.matches()) {
				if(!leftType.equalsIgnoreCase("boolean")) {
					return new Response(query,Status.FAILED,"Type mismatch on the where condition",null);
				}
				
				if(operator.equalsIgnoreCase("<")|| operator.equalsIgnoreCase("<=")|| operator.equalsIgnoreCase(">=")|| operator.equalsIgnoreCase(">")){
					return new Response(query,Status.FAILED,"Cannot compare Booleans with this operator",null);
				}
				//make entered value into boolean
				Boolean transferBoo = Boolean.parseBoolean(rightHandSide.strip());
				if(operator.equalsIgnoreCase("=")) {
					//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						if(transferBoo.equals(t.value().get(leftPointer)))
						result_table.state().put(t.key(), row);
					}
				}
			    else if(operator.equalsIgnoreCase("<>")){
			    	//for each row in the state
					for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
						LinkedList<Object> row = new LinkedList<>();
						//for each pointer
						for(int i=0; i< columnPointers.size(); i++) {
							//add the state value at the current index for pointer value 
							row.add(t.value().get(columnPointers.get(i)));
						}
						//fill return table with row
						if(!transferBoo.equals(t.value().get(leftPointer)))
						result_table.state().put(t.key(), row);
					}
				}
			}
			else {
				return new Response(query,Status.FAILED,"The right hand argument could not be defined",null);
			}
		
		
		}else if(matcher.group(3) == null) {
			
			//for each row in the state
			for(Entry<Object, List<Object>> t : db.tables().get(tableNameDb).state()) {
				LinkedList<Object> row = new LinkedList<>();
				//for each pointer
				for(int i=0; i< columnPointers.size(); i++) {
					//add the state value at the current index for pointer value 
					row.add(t.value().get(columnPointers.get(i)));
				}
				//fill return table with row
				result_table.state().put(t.key(), row);
			}
			
		}else {
			return new Response(query, Status.FAILED, "The table's state was unable to build", null);
		}
		
		db.tables().put("_select", result_table);
	
		return new Response(query, Status.SUCCESSFUL, null, result_table);
	}
}
