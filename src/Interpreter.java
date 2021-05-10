import java.io.*;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Comparator;

public class Interpreter{
	static PrintStream o;
	static PrintStream o2;
	static PrintStream console;
	static boolean isDouble = false;
	public static void main(String[] args) throws FileNotFoundException, IOException{
		if(args.length != 4){
			System.out.println("Input must be 4 files\nUsage java Interpreter classes.txt classoutput expression.txt expressionoutput");
			return;
		}
		//set up files
		File classFile = new File(args[0]);
		BufferedReader brClass = new BufferedReader(new FileReader(classFile));
		File expFile = new File(args[2]);
		BufferedReader brExp = new BufferedReader(new FileReader(expFile));
		o = new PrintStream(new File(args[1]));
		o2 = new PrintStream(new File(args[3]));
		console = System.out;
		//get parse tree
		multipleClassDef completeParse = Parser.getParse(brClass, o);
		//start paring the expression file
		String token = LexAnalyzer.getToken(brExp);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			eval(brExp,o2,console,completeParse,null);
		}
		else{
			//error invalid start
			System.setOut(o2);
			System.out.println(token_split[0] + " Error, invalid start of expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, invalid start of expression");
			System.exit(0);
		}
	}
	
	static String eval(BufferedReader brExp,PrintStream o2,PrintStream console,multipleClassDef completeParse, String workingClassName) throws IOException{
		String token = LexAnalyzer.getToken(brExp);
		String[] token_split = token.split(" ");
		//begin parse
		//go to expression function
		//arith
		if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
			System.setOut(o2);
			double result = arithExp(brExp,o2,token_split[0],completeParse,workingClassName);
			if(isDouble){
				System.out.println(result);
				System.setOut(console);
				System.out.println(result);
				return Double.toString(result);
			}
			else{
				System.out.println((int)result);
				System.setOut(console);
				System.out.println((int)result);
				return Integer.toString((int)result);
			}
		}
		//boolean
		else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
			boolean results = boolExp(brExp,o2,token_split[0],completeParse,workingClassName);
			System.setOut(o2);
			System.out.println(results);
			System.setOut(console);
			System.out.println(results);
			return Boolean.toString(results);
		}
		//comparison
		else if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
			boolean results = compExp(brExp,o2,token_split[0],completeParse,workingClassName);
			System.setOut(o2);
			System.out.println(results);
			System.setOut(console);
			System.out.println(results);
			return Boolean.toString(results);
		}
		//condition
		else if(token_split[0].equals("if")){
			double results = condExp(brExp,o2,token_split[0],completeParse,workingClassName);
			System.setOut(o2);
			System.out.println(results);
			System.setOut(console);
			System.out.println(results);
			return Double.toString(results);
		}
		//constructor
		else if(token_split[1].equals("id")){
			String results = constExp(brExp,o2,token_split[0],completeParse,workingClassName);
			System.setOut(o2);
			System.out.println(results);
			System.setOut(console);
			System.out.println(results);
			return results;
		}
		//field getter
		else if(token_split[0].equals(".")){
			String results = fieldExp(brExp,o2,token_split[0],completeParse,workingClassName);
			System.setOut(o2);
			System.out.println(results);
			System.setOut(console);
			System.out.println(results);
			return results;
		}
		//error
		else{
			System.setOut(o2);
			System.out.println(token_split[0] + " Error, couldn't find valid parse for expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, couldn't find valid parse for expression");
			System.exit(0);
		}
	return "error didn't return a valid result";
	}
	
	static double arithExp(BufferedReader br, PrintStream fileOut, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get arthimatic type
		double unit1 = -1;
		double unit2 = -1;
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
				unit1 = arithExp(br,fileOut,token_split[0],findClass,workingClassName);
				if(unit1 % 1 != 0){
					isDouble = true;
				}
			}
			else if(token_split[0].equals(".")){
				String fieldParse = fieldExp(br,fileOut,readToken,findClass,workingClassName);
				unit1 = Double.parseDouble(fieldParse);
				if(unit1 % 1 != 0){
					isDouble = true;
				}
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else if(token_split[0].equals("if")){
				unit1 = condExp(br,fileOut,token_split[0],findClass,workingClassName);
				if(unit1 % 1 != 0){
					isDouble = true;
				}
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression");
				System.exit(0);
			}
		}
		else{
			//first unit is number
			try{
				unit1 = Double.parseDouble(token_split[0]);
				if(unit1 % 1 != 0){
					isDouble = true;
				}
			}
			catch(Exception e){
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression");
				System.exit(0);
			}
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
				unit2 = arithExp(br,fileOut,token_split[0],findClass,workingClassName);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
			}
			else if(token_split[0].equals("if")){
				unit2 = condExp(br,fileOut,token_split[0],findClass,workingClassName);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
			}
			else if(token_split[0].equals(".")){
				String fieldParse = fieldExp(br,fileOut,readToken,findClass,workingClassName);
				unit2 = Double.parseDouble(fieldParse);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression");
				System.exit(0);
			}
		}
		else{
			//first unit is number
			try{
				unit2 = Double.parseDouble(token_split[0]);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
			}
			catch(Exception e){
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression");
				System.exit(0);
			}
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(!token_split[0].equals(")")){
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected expression close during arithmatic expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected expression close during arithmatic expression");
			System.exit(0);
		}
		if(readToken.equals("+")){
			//System.out.println("add:" + (unit1) + " " +(unit2) + " " + (unit1 + unit2));
			return unit1 + unit2;
		}
		else if(readToken.equals("*")){
			//System.out.println("times:" + (unit1) + " " +(unit2) + " " + (unit1 * unit2));
			return unit1 * unit2;
		}
		else if(readToken.equals("/")){
			//System.out.println("divide:" + (unit1) + " " +(unit2) + " " + (unit1 / unit2));
			if(unit2 == 0){
				System.setOut(fileOut);
				System.out.println("Error, divide by 0");
				System.setOut(console);
				System.out.println("Error, divide by 0");
				System.exit(0);
			}
			return unit1 / unit2;
		}
		else if(readToken.equals("-")){
			//System.out.println("minus:" + (unit1) + " " +(unit2) + " " + (unit1 - unit2));
			return unit1 - unit2;
		}
		else{
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected result to return during arithmatic expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during arithmatic expression");
			System.exit(0);
		}
		return -999999;
	}
	
	static boolean boolExp(BufferedReader br, PrintStream fileOut, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get arthimatic type
		boolean unit1 = false;
		boolean unit2 = false;
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
				unit1 = compExp(br,fileOut,token_split[0],findClass,workingClassName);
			}
			else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
				unit1 = boolExp(br,fileOut,token_split[0],findClass,workingClassName);
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected comparison expression during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected comparison expression during boolean expression");
				System.exit(0);
			}
		}
		else{
			//first unit is boolean
			try{
				unit1 = Boolean.parseBoolean(token_split[0]);
			}
			catch(Exception e){
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.exit(0);
			}
		}
		if(readToken.equals("!")){
			//System.out.println("divide:" + (unit1) + " " +(unit2) + " " + (unit1 / unit2));
			return !unit1;
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
				unit2 = compExp(br,fileOut,token_split[0],findClass,workingClassName);
			}
			else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
				unit2 = boolExp(br,fileOut,token_split[0],findClass,workingClassName);
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected comparison expression during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected comparison expression during boolean expression");
				System.exit(0);
			}
		}
		else{
			//first unit is boolean
			try{
				unit2 = Boolean.parseBoolean(token_split[0]);
			}
			catch(Exception e){
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.exit(0);
			}
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(!token_split[0].equals(")")){
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected expression close during boolean expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected expression close during boolean expression");
			System.exit(0);
		}
		if(readToken.equals("|")){
			//System.out.println("add:" + (unit1) + " " +(unit2) + " " + (unit1 + unit2));
			return unit1 || unit2;
		}
		else if(readToken.equals("&")){
			//System.out.println("times:" + (unit1) + " " +(unit2) + " " + (unit1 * unit2));
			return unit1 && unit2;
		}
		else{
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected result to return during boolean expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during boolean expression");
			System.exit(0);
		}
		return false;
	}
	
	static boolean compExp(BufferedReader br, PrintStream fileOut, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get arthimatic type
		double unit1 = -1;
		double unit2 = -1;
		boolean unit1b = false;
		boolean unit2b = false;
		String unit1Obj = "";
		String unit2Obj = "";
		
		boolean unit1Bool = false;
		boolean unit2Bool = false;
		boolean unit1Double = false;
		boolean unit2Double = false;
		boolean unit1Object = false;
		boolean unit2Object = false;
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
				unit1 = arithExp(br,fileOut,token_split[0],findClass,workingClassName);
				if(unit1 % 1 != 0){
					unit1Double = true;
				}
			}
			else if(readToken.equals("=") && (token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("="))){
				unit1b = compExp(br,fileOut,token_split[0],findClass,workingClassName);
				unit1Bool = true;
			}
			else if(token_split[1].equals("id")){
				unit1Obj = constExp(br,fileOut,token_split[0],findClass,workingClassName);
				unit1Object = true;
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during comparison expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during comparison expression");
				System.exit(0);
			}
		}
		else{
			//first unit is number
			try{
				unit1 = Double.parseDouble(token_split[0]);
				if(unit1 % 1 != 0){
					unit1Double = true;
				}
			}
			catch(Exception e){
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected number during comparison expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during comparison expression");
				System.exit(0);
			}
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
				unit2 = arithExp(br,fileOut,token_split[0],findClass,workingClassName);
				if(unit2 % 1 != 0){
					unit2Double = true;
				}
			}
			else if(readToken.equals("=") && (token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("="))){
				unit2b = compExp(br,fileOut,token_split[0],findClass,workingClassName);
				unit2Bool = true;
			}
			else if(token_split[1].equals("id")){
				unit2Obj = constExp(br,fileOut,token_split[0],findClass,workingClassName);
				unit2Object = true;
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during comparison expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during comparison expression");
				System.exit(0);
			}
		}
		else{
			//first unit is number
			try{
				unit2 = Double.parseDouble(token_split[0]);
				if(unit2 % 1 != 0){
					unit2Double = true;
				}
			}
			catch(Exception e){
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected number during comparison expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during comparison expression");
				System.exit(0);
			}
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(!token_split[0].equals(")")){
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected expression close during comparison expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected expression close during comparison expression");
			System.exit(0);
		}
		if(readToken.equals("<")){
			//System.out.println("<:" + (unit1) + " " +(unit2) + " " + (unit1 < unit2));
			return unit1 < unit2;
		}
		else if(readToken.equals(">")){
			//System.out.println(">:" + (unit1) + " " +(unit2) + " " + (unit1 > unit2));
			return unit1 > unit2;
		}
		else if(readToken.equals("<=")){
			//System.out.println("<=:" + (unit1) + " " +(unit2) + " " + (unit1 <= unit2));
			return unit1 <= unit2;
		}
		else if(readToken.equals(">=")){
			//System.out.println(">=:" + (unit1) + " " +(unit2) + " " + (unit1 >= unit2));
			return unit1 >= unit2;
		}
		else if(readToken.equals("=")){
			//System.out.println("equal:" + (unit1) + " " +(unit2) + " " + (unit1 == unit2));
			if(unit1Bool && unit2Bool){
				return unit1b == unit2b;
			}
			else if(unit1Double && unit2Double){
				return unit1 == unit2;
			}
			else if(unit1Object && unit2Object){
				return unit1Obj.equals(unit2Obj);
			}
			else{
				return false;
			}
		}
		else{
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected result to return during comparison expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during comparison expression");
			System.exit(0);
		}
		return false;
	}
	
	static double condExp(BufferedReader br, PrintStream fileOut, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get arthimatic type
		double unit1 = -1;
		double unit2 = -1;
		boolean unit1b = false;
		boolean unit2b = false;
		
		boolean unit1Double = false;
		boolean unit2Double = false;
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
				unit1b = compExp(br,fileOut,token_split[0],findClass,workingClassName);
			}
			else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
				unit1b = boolExp(br,fileOut,token_split[0],findClass,workingClassName);
			}
			else{
				//error
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
				System.exit(0);
			}
		}
		else{
			//comp expected
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
			System.exit(0);
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		//get first number for true statement 
		try{
			unit1 = Double.parseDouble(token_split[0]);
			if(unit1 % 1 != 0){
				unit1Double = true;
			}
		}
		catch(Exception e){
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected number during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected number during conditional check");
			System.exit(0);
		}
		//get second number for false statement
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		try{
			unit2 = Double.parseDouble(token_split[0]);
			if(unit2 % 1 != 0){
				unit2Double = true;
			}
		}
		catch(Exception e){
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected number during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected number during conditional check");
			System.exit(0);
		}
		
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(!token_split[0].equals(")")){
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected expression close during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected expression close during conditional check");
			System.exit(0);
		}
		if(unit1b){
			return unit1;	
		}
		if(!unit1b){
			return unit2;
		}
		else{
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, expected result to return during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during conditional check");
			System.exit(0);
		}
		return -999999;
	}

	static String constExp(BufferedReader br, PrintStream fileOut, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		boolean foundClass = false;
		String builtObject = "";
		while(findClass.multiclassdef != null){
			if(findClass.classInfo.className.equals(readToken)){
				foundClass = true;
				builtObject = findClass.classInfo.className + " object: {";
				for(int i=0;i<findClass.classInfo.fields.size();i++){
					if(i!=0){
						builtObject += ", ";
					}
					String token = LexAnalyzer.getToken(br);
					String[] token_split = token.split(" ");
					if(token_split[0].equals("(")){
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
						//arith
						if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
							double result = arithExp(br,fileOut,token_split[0],findClass,workingClassName);
							if(result % 1 != 0){
								builtObject += findClass.classInfo.fields.get(i) + "=" + Double.toString(result);
							}
							else{
								builtObject += findClass.classInfo.fields.get(i) + "=" + Integer.toString((int)result);
							}
						}
						else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
							boolean result = boolExp(br,fileOut,token_split[0],findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + Boolean.toString(result);
						}
						else if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
							boolean result = compExp(br,fileOut,token_split[0],findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + Boolean.toString(result);
						}
						else if(token_split[0].equals("if")){
							double result = condExp(br,fileOut,token_split[0],findClass,workingClassName);
							if(result % 1 != 0){
								builtObject += findClass.classInfo.fields.get(i) + "=" + Double.toString(result);
							}
							else{
								builtObject += findClass.classInfo.fields.get(i) + "=" + Integer.toString((int)result);
							}
						}
						else if(token_split[1].equals("id")){
							String result = constExp(br, fileOut, token_split[0], findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + result;
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
							if(!token_split[0].equals(")")){
								System.setOut(fileOut);
								System.out.println(token_split[0] + " Error, expected end of expression during constructor");
								System.setOut(console);
								System.out.println(token_split[0] + " Error, expected end of expression during constructor");
								System.exit(0);
							}
						}
						else{
							System.setOut(fileOut);
							System.out.println(token_split[0] + " Error, expected expression to parse during constructor");
							System.setOut(console);
							System.out.println(token_split[0] + " Error, expected expression to parse during constructor");
							System.exit(0);
						}
					}
					else{
						try{
							double unit1 = Double.parseDouble(token_split[0]);
							builtObject += findClass.classInfo.fields.get(i) + "=" + token_split[0];
						}
						catch(Exception e){
							if(token_split[0].equals("null")){
								builtObject += findClass.classInfo.fields.get(i) + "=" + token_split[0];
							}
							else{
								System.setOut(fileOut);
								System.out.println(token_split[0] + " Error, expected number during constructor");
								System.setOut(console);
								System.out.println(token_split[0] + " Error, expected number during constructor");
								System.exit(0);
							}
						}
					}
				}
				builtObject += "}";
				return builtObject;
			}
			findClass = findClass.multiclassdef;
		}
		if(!foundClass){
			System.setOut(fileOut);
			System.out.println(readToken + " Error, class not found during constructor");
			System.setOut(console);
			System.out.println(readToken + " Error, class not found during constructor");
			System.exit(0);
		}
		return "";
	}
	//project 4 modified for user functions 
	static String fieldExp(BufferedReader br, PrintStream fileOut, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[1].equals("id")){
				String classInfo = constExp(br,fileOut,token_split[0],findClass,workingClassName);
				workingClassName = classInfo;
				//System.out.println("class gotten:" + classInfo);
				multipleClassDef workingClass = findClass;
				while(!workingClass.classInfo.className.equals(token_split[0])){
					workingClass = workingClass.multiclassdef;
				}
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				if(token_split[0].equals("(")){
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					if(token_split[1].equals("id")){
						//gotten function/variable to return
						//if parameter gotten is in fields
						System.out.println(token_split[0]);
						System.out.println(workingClass.classInfo.funMap);
						System.out.println(workingClass.classInfo.funMap.containsKey(token_split[0]));
						if(classInfo.contains(token_split[0])){
							String parametersOfMain = "";
							int braceCount = 0;
							for(int i=0;i<classInfo.length();i++){
								char temp = classInfo.charAt(i);
								if(temp == '{'){
									braceCount++;
									do{
										i++;
										temp = classInfo.charAt(i);
										
										if(temp == '{'){
											braceCount++;
										}
										if(temp == '}'){
											braceCount--;
											if(braceCount == 0){
												break;
											}
										}
										parametersOfMain += temp;
									}while(braceCount != 0);
								}
							}
							//System.out.println("main parameters:" + parametersOfMain);
							//start getting individual parameters
							String parameterName = "";
							String parameterValue = "";
							braceCount = 0;
							for(int i=0;i<parametersOfMain.length();i++){
								char temp = parametersOfMain.charAt(i);
								if(temp == '='){
									int innerBrace = 0;
									i++;
									while(temp != ',' || innerBrace != 0){
										temp = parametersOfMain.charAt(i);
										if(temp == '{'){
											innerBrace++;
										}
										if(temp == '}'){
											innerBrace--;
										}
										if(innerBrace == 0 && temp == ','){
											break;
										}
										parameterValue += temp;
										//System.out.println("param val so far:" + parameterValue);
										i++;
										if(i == parametersOfMain.length()){
											break;
										}
									}
									if(parameterName.equals(token_split[0])){
										return parameterValue;
									}
									i += 2;
									parameterName = "";
									parameterValue = "";
									temp = parametersOfMain.charAt(i);
								}
								parameterName += temp;
							}
						}
						//function name found
						else if(workingClass.classInfo.funMap.containsKey(token_split[0])){
							//function was found, start getting its information
							int funParamLength = workingClass.classInfo.funMap.get(token_split[0]).size();
							LinkedList<String> funParams = workingClass.classInfo.funMap.get(token_split[0]);
							String funBody = workingClass.classInfo.funBodyMap.get(token_split[0]);
							for(int i=0;i<funParamLength;i++){
								String result = "";
								token = LexAnalyzer.getToken(br);
								token_split = token.split(" ");
								System.out.println("function part to replace:"+funParams.get(i)+" with "+token_split[0]);
								if(token_split[0].equals("(")){
									result = eval(br,fileOut,console,workingClass,workingClassName);
									funBody = funBody.replace(funParams.get(i), result);
								}
								else{
									funBody = funBody.replace(funParams.get(i), token_split[0]);
								}
							}
							System.out.println("Function body made from peices:" + funBody);
							Reader functionToParse = new StringReader(funBody);
							BufferedReader subParser = new BufferedReader(functionToParse);
							LexAnalyzer.getToken(subParser);
							String subResult = eval(subParser,fileOut,fileOut,workingClass,workingClassName);
							System.out.println("results of output:"+ subResult);
							return subResult;
						}
						else{
							System.setOut(fileOut);
							System.out.println(token_split[0] + " Error, couldn't find symbol during field expresion");
							System.setOut(console);
							System.out.println(token_split[0] + " Error, couldn't find symbol during field expresion");
							System.exit(0);
						}
					}
					
					else{
						//error
						System.setOut(fileOut);
						System.out.println(token_split[0] + " Error, expected token field expresion");
						System.setOut(console);
						System.out.println(token_split[0] + " Error, expected token field expresion");
						System.exit(0);
					}
				}
				else{
					//error
					System.setOut(fileOut);
					System.out.println(token_split[0] + " Error, start of class construction expected field expresion");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, start of class construction expected field expresion");
					System.exit(0);
				}
			}
			else if(token_split[0].equals(".")){
				String parametersOfMain = fieldExp(br,fileOut,readToken,findClass,workingClassName);
				//first end paren
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				//second end paren
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				//start paren
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				//token
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				if(token_split[1].equals("id")){
					String parameterName = "";
					String parameterValue = "";
					for(int i=0;i<parametersOfMain.length();i++){
						char temp = parametersOfMain.charAt(i);
						if(temp == '='){
							int innerBrace = 0;
							i++;
							while(temp != ',' || innerBrace != 0){
								temp = parametersOfMain.charAt(i);
								if(temp == '{'){
									innerBrace++;
								}
								if(temp == '}'){
									innerBrace--;
								}
								if(innerBrace == 0 && temp == ','){
									break;
								}
								parameterValue += temp;
								//System.out.println("param val so far:" + parameterValue);
								i++;
								if(i == parametersOfMain.length()){
									break;
								}
							}
							if(parameterName.equals(token_split[0])){
								return parameterValue;
							}
							i += 2;
							parameterName = "";
							parameterValue = "";
							temp = parametersOfMain.charAt(i);
						}
						parameterName += temp;
					}
					
					System.setOut(fileOut);
					System.out.println(token_split[0] + " Error, couldn't find symbol field expresion");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, couldn't find symbol field expresion");
					System.exit(0);
				}
				else{
					System.setOut(fileOut);
					System.out.println(token_split[0] + " Error, expected symbol name field expresion");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, expected symbol name field expresion");
					System.exit(0);
				}
			}
			else{
				System.setOut(fileOut);
				System.out.println(token_split[0] + " Error, expected class name field expresion");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected class name field expresion");
				System.exit(0);
			}
		}
		else if(token_split[0].equals("this")){
			System.out.println("got 'this' statement");
			return parseThis(br,fileOut,findClass,workingClassName);
		}
		else{
			//error
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, start of expression expected field expresion");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, start of expression expected field expresion");
			System.exit(0);
		}
		return "";
	}
	static String parseThis(BufferedReader br, PrintStream fileOut, multipleClassDef findClass, String workingClassName) throws IOException{
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
		}
		if(workingClassName.contains(token_split[0])){
			String classInfo = workingClassName;
			String parametersOfMain = "";
			int braceCount = 0;
			for(int i=0;i<classInfo.length();i++){
				char temp = classInfo.charAt(i);
				if(temp == '{'){
					braceCount++;
					do{
						i++;
						temp = classInfo.charAt(i);
						
						if(temp == '{'){
							braceCount++;
						}
						if(temp == '}'){
							braceCount--;
							if(braceCount == 0){
								break;
							}
						}
						parametersOfMain += temp;
					}while(braceCount != 0);
				}
			}
			//System.out.println("main parameters:" + parametersOfMain);
			//start getting individual parameters
			String parameterName = "";
			String parameterValue = "";
			braceCount = 0;
			for(int i=0;i<parametersOfMain.length();i++){
				char temp = parametersOfMain.charAt(i);
				if(temp == '='){
					int innerBrace = 0;
					i++;
					while(temp != ',' || innerBrace != 0){
						temp = parametersOfMain.charAt(i);
						if(temp == '{'){
							innerBrace++;
						}
						if(temp == '}'){
							innerBrace--;
						}
						if(innerBrace == 0 && temp == ','){
							break;
						}
						parameterValue += temp;
						//System.out.println("param val so far:" + parameterValue);
						i++;
						if(i == parametersOfMain.length()){
							break;
						}
					}
					if(parameterName.equals(token_split[0])){
						return parameterValue;
					}
					i += 2;
					parameterName = "";
					parameterValue = "";
					temp = parametersOfMain.charAt(i);
				}
				parameterName += temp;
			}
		}
		else if(findClass.classInfo.funMap.containsKey(token_split[0])){
			String funBody = findClass.classInfo.funBodyMap.get(token_split[0]);
			Reader bodyParse = new StringReader(funBody);
			BufferedReader bodyBr = new BufferedReader(bodyParse);
			//get next token
			token = LexAnalyzer.getToken(bodyBr);
			token_split = token.split(" ");
			if(token_split[0].equals("(")){
				//function body is an expression
				return eval(bodyBr,fileOut,console,findClass,workingClassName);
			}
			else{
				//function body is a value
				return token_split[0];
			}
		}
		else{
			//error
			System.setOut(fileOut);
			System.out.println(token_split[0] + " Error, failed to parse 'this' expresion");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, failed to parse 'this' expresion");
			System.exit(0);
		}
		return "error";
	}
}




























