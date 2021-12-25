package apps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import types.Entry;
import types.Response;
import types.Table;

/**
 * Implements a user console for
 * interacting with a database.
 * <p>
 * Additional protocols may be added.
 */
public class Console {
	/**
	 * The entry point for execution
	 * with user input/output.
	 *
	 * @param args ignored.
	 */
	public static boolean transactionCheck = false;
	
	
	
	public static void main(String[] args) {
		try (
			final Database db = new Database();
			final Scanner in = new Scanner(System.in);
			final PrintStream out = System.out;
			final Database backup = new Database();
		)
		{
			while(true) {
			out.print(">> ");

			String text = in.nextLine().strip();
			
			if(text.equalsIgnoreCase("EXIT"))
				break;

			List<String> queries = Arrays.asList(text.split(";"));

			List<Response> responses = db.interpret(queries);

			for (Response res: responses) {
			out.println("Query:   " + res.query().strip());
			out.println("Status:  " + res.status());
			out.println("Details: " + res.details());
			out.println("Table:   " + res.table());
		/*	
			if(res.table() == null) {
				out.println("Table:   " + res.table());
				continue;
			}
			
			out.println("Table:   " + res.table());

			String tableName = res.table().getTableName(); 
			List<String> columnNames = res.table().getColumnNames();
			List<String> columnTypes = res.table().getColumnTypes();
			String primaryName = res.table().getPrimaryName();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("   ");
			for (int i = 0; i < tableName.length(); i++) {
				sb.append("_");
			}
		  sb.append("\n")
			.append(" / ")
			.append(tableName)
			.append(" \\ \n")
			.append("+");
			
			for (int i = 0; i < columnNames.size(); i++) 
				sb.append("-".repeat(30));
			
			sb.append("+\n|");
			
			
			for(String s : columnNames) {
				
				int index = columnNames.indexOf(s);
				
				if(columnTypes.get(index).equalsIgnoreCase("string")) {
					
					if(s.equals(primaryName)) {
						sb.append(String.format("%-30s", s + "*"));
						  	
					}else {
						sb.append(String.format("%-30s", s));
					}
					
					
						  
					
				}else if(columnTypes.get(index).equalsIgnoreCase("integer")) {
					if(s.equals(primaryName)) {
						 sb.append(String.format("%29s", s + "*"));
						   
					}else {
						 sb.append(String.format("%29s", s));
					}
					
					     
					
					
				}else if(columnTypes.get(index).equalsIgnoreCase("boolean")){
					     
					
					if(s.equals(primaryName)) {
						sb.append(String.format("%-29s", s + "*"));
						  	
					}else {
						sb.append(String.format("%-29s", s));
					}
					
					}
				
				
					sb.append("|");
				
					}
			
			sb.append("\n+");
			
			for (int i = 0; i < columnNames.size(); i++) 
				sb.append("=".repeat(30));
			
			sb.append("+\n");
			
			for (Entry<Object,List<Object>> entry: db.tables().get(tableName).state()) {
				Object row[] =  entry.value().toArray();
				sb.append("|");
				for(int i = 0; i < row.length; i++) {
					if(row[i] == null) {
						sb.append(String.format("%29s", " "));
						sb.append("|");
						continue;
					}
					
					String s = (String) row[i].toString();
					
					if(columnTypes.get(i).equalsIgnoreCase("string")) {
						
						if(s.length() >= 30) {
							String truncate = s.substring(0,20) + "(. . .)";
							if(row[i].toString().contains("\""))
								
								sb.append(String.format("%-30s", truncate));
							else 
								sb.append(String.format("\"%-29s", truncate));
							sb.append("|");
							continue;
						}
						
						if(row[i].toString().contains("\""))
							
							sb.append(String.format("%-30s", s));
						else 
							sb.append(String.format("\"%-29s", s + "\""));
					
						
						
						
					}else if(columnTypes.get(i).equalsIgnoreCase("integer")) {
						
							 sb.append(String.format("%29s", s));
				
					}else if(columnTypes.get(i).equalsIgnoreCase("boolean")){
					
							sb.append(String.format("%-29s", s ));
					
						}else {
						
							sb.append(String.format("%29s", " "));
						
						}
					
					
						sb.append("|");
				}
				sb.append("\n");
			}
			
			sb.append("+");
			
			for (int i = 0; i < columnNames.size(); i++) 
				sb.append("=".repeat(30));
			
			sb.append("+\n");
				
			
			out.println(sb);
			
			
			
			out.println();
			*/
		}
			
		}
		
	}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
