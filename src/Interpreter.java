import java.io.*;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Comparator;

public class Interpreter{
	static PrintStream classOutput;
	static PrintStream resultsOutput;
	static PrintStream console;
	static PrintStream debug;
	static boolean isDouble = false;
	static multipleClassDef fullClassesTree;
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
		classOutput = new PrintStream(new File(args[1]));
		resultsOutput = new PrintStream(new File(args[3]));
		debug = new PrintStream("debug.txt");
		console = System.out;
		//get parse tree
		multipleClassDef completeParse = Parser.getParse(brClass, classOutput, debug);
		fullClassesTree = completeParse;
		//start paring the expression file
		String token = LexAnalyzer.getToken(brExp);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			String results = eval(brExp,completeParse,null);
			System.setOut(resultsOutput);
			System.out.println(results);
			System.setOut(console);
			System.out.println(results);
		}
		else{
			//error invalid start
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, invalid start of expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, invalid start of expression");
			System.exit(0);
		}
	}
	
	static String eval(BufferedReader brExp, multipleClassDef completeParse, String workingClassName) throws IOException{
		String token = LexAnalyzer.getToken(brExp);
		String[] token_split = token.split(" ");
		//begin parse
		//go to expression function
		//arith
		if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
			double result = arithExp(brExp,token_split[0],completeParse,workingClassName);
			if(isDouble){
				System.setOut(debug);
				System.out.println(result);
				System.setOut(resultsOutput);
				return Double.toString(result);
			}
			else{
				System.setOut(debug);
				System.out.println((int)result);
				System.setOut(resultsOutput);
				return Integer.toString((int)result);
			}
		}
		//boolean
		else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
			boolean results = boolExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(results);
			return Boolean.toString(results);
		}
		//comparison
		else if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
			boolean results = compExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(results);
			return Boolean.toString(results);
		}
		//condition
		else if(token_split[0].equals("if")){
			String results = condExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(results);
			return results;
		}
		//constructor
		else if(token_split[1].equals("id")){
			String results = constExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(results);
			return results;
		}
		//field getter
		else if(token_split[0].equals(".")){
			String results = fieldExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(results);
			return results;
		}
		//error
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, couldn't find valid parse for expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, couldn't find valid parse for expression");
			System.exit(0);
		}
	return "error didn't return a valid result";
	}
	
	static double arithExp(BufferedReader br, String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
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
				unit1 = arithExp(br,token_split[0],findClass,workingClassName);
				if(unit1 % 1 != 0){
					isDouble = true;
				}
			}
			else if(token_split[0].equals(".")){
				String fieldParse = fieldExp(br,readToken,findClass,workingClassName);
				unit1 = Double.parseDouble(fieldParse);
				if(unit1 % 1 != 0){
					isDouble = true;
				}
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else if(token_split[0].equals("if")){
				unit1 = Double.parseDouble(condExp(br,token_split[0],findClass,workingClassName));
				if(unit1 % 1 != 0){
					isDouble = true;
				}
			}
			else{
				//error
				System.setOut(resultsOutput);
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
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			catch(Exception e){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit1");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit1");
				System.exit(0);
			}
		}
		System.setOut(debug);
		System.out.println("arithmatic start second token:"+token_split[0]);
		System.setOut(resultsOutput);
		//gets second token
		if(token_split[0].equals("(")){
			//second unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			System.setOut(debug);
			System.out.println("token in second arith exp expression:"+token_split[0]);
			System.setOut(resultsOutput);
			if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
				unit2 = arithExp(br,token_split[0],findClass,workingClassName);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
			}
			else if(token_split[0].equals("if")){
				unit2 = Double.parseDouble(condExp(br,token_split[0],findClass,workingClassName));
				if(unit2 % 1 != 0){
					isDouble = true;
				}
			}
			else if(token_split[0].equals(".")){
				System.setOut(debug);
				System.out.println("arithmatic second token is dot");
				System.setOut(resultsOutput);
				String fieldParse = fieldExp(br,readToken,findClass,workingClassName);
				unit2 = Double.parseDouble(fieldParse);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else{
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression");
				System.exit(0);
			}
		}
		else{
			//second unit is number
			try{
				unit2 = Double.parseDouble(token_split[0]);
				if(unit2 % 1 != 0){
					isDouble = true;
				}
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				if(!token_split[0].equals(")")){
					System.setOut(resultsOutput);
					System.out.println(token_split[0] + " Error, expected expression close during arithmatic expression");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, expected expression close during arithmatic expression");
					System.exit(0);
				}
			}
			catch(Exception e){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit2");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit2");
				System.exit(0);
			}
		}
		if(readToken.equals("+")){
			System.setOut(debug);
			System.out.println("add:" + (unit1) + " " +(unit2) + " " + (unit1 + unit2));
			System.setOut(resultsOutput);
			return unit1 + unit2;
		}
		else if(readToken.equals("*")){
			System.setOut(debug);
			System.out.println("times:" + (unit1) + " " +(unit2) + " " + (unit1 * unit2));
			System.setOut(resultsOutput);
			return unit1 * unit2;
		}
		else if(readToken.equals("/")){
			System.setOut(debug);
			System.out.println("divide:" + (unit1) + " " +(unit2) + " " + (unit1 / unit2));
			System.setOut(resultsOutput);
			if(unit2 == 0){
				System.setOut(resultsOutput);
				System.out.println("Error, divide by 0");
				System.setOut(console);
				System.out.println("Error, divide by 0");
				System.exit(0);
			}
			return unit1 / unit2;
		}
		else if(readToken.equals("-")){
			System.setOut(debug);
			System.out.println("minus:" + (unit1) + " " +(unit2) + " " + (unit1 - unit2));
			System.setOut(resultsOutput);
			return unit1 - unit2;
		}
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected result to return during arithmatic expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during arithmatic expression");
			System.exit(0);
		}
		return -999999;
	}
	
	static boolean boolExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
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
				unit1 = compExp(br,token_split[0],findClass,workingClassName);
			}
			else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
				unit1 = boolExp(br,token_split[0],findClass,workingClassName);
			}
			else{
				//error
				System.setOut(resultsOutput);
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
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.exit(0);
			}
		}
		if(readToken.equals("!")){
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
				unit2 = compExp(br,token_split[0],findClass,workingClassName);
			}
			else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
				unit2 = boolExp(br,token_split[0],findClass,workingClassName);
			}
			else{
				//error
				System.setOut(resultsOutput);
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
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.exit(0);
			}
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(!token_split[0].equals(")")){
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected expression close during boolean expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected expression close during boolean expression");
			System.exit(0);
		}
		if(readToken.equals("|")){
			return unit1 || unit2;
		}
		else if(readToken.equals("&")){
			return unit1 && unit2;
		}
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected result to return during boolean expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during boolean expression");
			System.exit(0);
		}
		return false;
	}
	
	static boolean compExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
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
		System.setOut(debug);
		System.out.println("first token from comp expression:"+token_split[0]);
		System.setOut(resultsOutput);
		if(token_split[0].equals("(")){
			//first unit is new expression
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
				unit1 = arithExp(br,token_split[0],findClass,workingClassName);
				unit1Double = true;
			}
			else if(readToken.equals("=") && (token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("="))){
				unit1b = compExp(br,token_split[0],findClass,workingClassName);
				unit1Bool = true;
			}
			else if(token_split[1].equals("id")){
				unit1Obj = constExp(br,token_split[0],findClass,workingClassName);
				unit1Object = true;
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else{
				//error
				System.setOut(resultsOutput);
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
				unit1Double = true;
			}
			catch(Exception e){
				System.setOut(resultsOutput);
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
				unit2 = arithExp(br,token_split[0],findClass,workingClassName);
				unit2Double = true;
			}
			else if(readToken.equals("=") && (token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("="))){
				unit2b = compExp(br,token_split[0],findClass,workingClassName);
				unit2Bool = true;
			}
			else if(token_split[1].equals("id")){
				unit2Obj = constExp(br,token_split[0],findClass,workingClassName);
				unit2Object = true;
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			else{
				//error
				System.setOut(resultsOutput);
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
				unit2Double = true;
			}
			catch(Exception e){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected number during comparison expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during comparison expression");
				System.exit(0);
			}
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(!token_split[0].equals(")")){
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected expression close during comparison expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected expression close during comparison expression");
			System.exit(0);
		}
		if(readToken.equals("<")){
			System.setOut(debug);
			System.out.println("<:" + (unit1) + " " +(unit2) + " " + (unit1 < unit2));
			System.setOut(resultsOutput);
			return unit1 < unit2;
		}
		else if(readToken.equals(">")){
			System.setOut(debug);
			System.out.println(">:" + (unit1) + " " +(unit2) + " " + (unit1 > unit2));
			System.setOut(resultsOutput);
			return unit1 > unit2;
		}
		else if(readToken.equals("<=")){
			System.setOut(debug);
			System.out.println("<=:" + (unit1) + " " +(unit2) + " " + (unit1 <= unit2));
			System.setOut(resultsOutput);
			return unit1 <= unit2;
		}
		else if(readToken.equals(">=")){
			System.setOut(debug);
			System.out.println(">=:" + (unit1) + " " +(unit2) + " " + (unit1 >= unit2));
			System.setOut(resultsOutput);
			return unit1 >= unit2;
		}
		else if(readToken.equals("=")){
			System.setOut(debug);
			System.out.println("equal:" + (unit1) + " " +(unit2) + " " + (unit1 == unit2));
			System.out.println("unit1bool:"+unit1Bool+"\nunit2bool:"+unit2Bool+"\nunit1Double:"+unit1Double+"\nunit2Double:"+unit2Double+"\nunit1Object:"+unit1Object+"\nunit2Object:"+unit2Object);
			System.setOut(resultsOutput);
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
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected result to return during comparison expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during comparison expression");
			System.exit(0);
		}
		return false;
	}
	
	static String condExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get arthimatic type
		String unit1 = "";
		String unit2 = "";
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
				unit1b = compExp(br,token_split[0],findClass,workingClassName);
				System.setOut(debug);
				System.out.println("compExp result in conditional:"+unit1b);
				System.setOut(resultsOutput);
			}
			else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
				unit1b = boolExp(br,token_split[0],findClass,workingClassName);
				System.setOut(debug);
				System.out.println("boolExp result in conditional:"+unit1b);
				System.setOut(resultsOutput);
			}
			else{
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
				System.exit(0);
			}
		}
		else{
			//comp expected
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
			System.exit(0);
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		//get first number for true statement 
		if(token_split[0].equals("(")){
			System.setOut(debug);
			System.out.println("Parsing in cond expression as unit1");
			System.setOut(resultsOutput);
			unit1 = eval(br,findClass,workingClassName);
		}
		else{
			unit1 = token_split[0];
		}
		//check if first statement is true, if so simply skip to end
		System.setOut(debug);
		System.out.println("is statement true:"+unit1b);
		System.setOut(resultsOutput);
		if(unit1b){
			return unit1;
		}
		else{
			//get second number for false statement
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			//start of function
			if(token_split[0].equals("(")){
				System.setOut(debug);
				System.out.println("Parsing in cond expression as unit2");
				System.setOut(resultsOutput);
				unit2 = eval(br,findClass,workingClassName);
			}
			else{
				unit2 = token_split[0];
			}
			//this will be a problem soon, but it works when removed so who know !
			/**
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected expression close during first conditional check");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected expression close during first conditional check");
				System.exit(0);
			}
			**/
		}
		System.setOut(debug);
		System.out.println("Value of unit1 in cond:"+unit1);
		System.out.println("Value of unit2 in cond:"+unit2);
		System.setOut(resultsOutput);
		if(!unit1b){
			return unit2;
		}
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected result to return during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during conditional check");
			System.exit(0);
		}
		return "";
	}

	static String constExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		boolean foundClass = false;
		String builtObject = "";
		multipleClassDef searcher = fullClassesTree;
		while(searcher.multiclassdef != null){
			if(searcher.classInfo.className.equals(readToken)){
				foundClass = true;
				findClass = searcher;
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
							double result = arithExp(br,token_split[0],findClass,workingClassName);
							if(result % 1 != 0){
								builtObject += findClass.classInfo.fields.get(i) + "=" + Double.toString(result);
							}
							else{
								builtObject += findClass.classInfo.fields.get(i) + "=" + Integer.toString((int)result);
							}
						}
						else if(token_split[0].equals("|") || token_split[0].equals("&") || token_split[0].equals("!")){
							boolean result = boolExp(br,token_split[0],findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + Boolean.toString(result);
						}
						else if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
							boolean result = compExp(br,token_split[0],findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + Boolean.toString(result);
						}
						else if(token_split[0].equals("if")){
							String result = condExp(br,token_split[0],findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + result;
						}
						else if(token_split[1].equals("id")){
							String result = constExp(br, token_split[0], findClass,workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + result;
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
							if(!token_split[0].equals(")")){
								System.setOut(resultsOutput);
								System.out.println(token_split[0] + " Error, expected end of expression during constructor");
								System.setOut(console);
								System.out.println(token_split[0] + " Error, expected end of expression during constructor");
								System.exit(0);
							}
						}
						else if(token_split[0].equals(".")){
							String result = fieldExp(br, token_split[0], findClass, workingClassName);
							builtObject += findClass.classInfo.fields.get(i) + "=" + result;
						}
						else{
							System.setOut(resultsOutput);
							System.out.println(token_split[0] + " Error, expected expression to parse during constructor");
							System.setOut(console);
							System.out.println(token_split[0] + " Error, expected expression to parse during constructor");
							System.exit(0);
						}
					}
					else{
						builtObject += findClass.classInfo.fields.get(i) + "=" + token_split[0];
					}
				}
				builtObject += "}";
				return builtObject;
			}
			searcher = searcher.multiclassdef;
		}
		if(!foundClass){
			System.setOut(resultsOutput);
			System.out.println(readToken + " Error, class not found during constructor");
			System.setOut(console);
			System.out.println(readToken + " Error, class not found during constructor");
			System.exit(0);
		}
		return "";
	}
	//project 4 modified for user functions 
	static String fieldExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[1].equals("id")){
				String classInfo = constExp(br,token_split[0],findClass,workingClassName);
				workingClassName = classInfo;
				System.setOut(debug);
				System.out.println("class gotten:" + classInfo);
				System.setOut(resultsOutput);
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
						System.setOut(debug);
						System.out.println(token_split[0]);
						System.out.println(workingClass.classInfo.funMap);
						System.out.println(workingClass.classInfo.funMap.containsKey(token_split[0]));
						System.setOut(resultsOutput);
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
							System.setOut(debug);
							System.out.println("main parameters:" + parametersOfMain);
							System.setOut(resultsOutput);
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
										System.setOut(debug);
										System.out.println("param val so far:" + parameterValue);
										System.setOut(resultsOutput);
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
							System.setOut(debug);
							System.out.println("Current function:"+funBody);
							System.setOut(resultsOutput);
							//replace values section
							for(int i=0;i<funParamLength;i++){
								while(funBody.contains(" this ")){
									funBody = funBody.replaceAll(" this ","  ("+workingClass.classInfo.className+") ");
									System.setOut(debug);
									System.out.println("current replacement of this:" + funBody);
									System.setOut(resultsOutput);
								}
								String result = "";
								token = LexAnalyzer.getToken(br);
								token_split = token.split(" ");
								System.setOut(debug);
								System.out.println("function part to replace:"+funParams.get(i)+" with "+token_split[0]);
								System.setOut(resultsOutput);
								if(token_split[0].equals("(")){
									result = eval(br,workingClass,workingClassName);
									funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
									while(funBody.contains(" "+funParams.get(i)+" ")){
										funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
									}
									System.setOut(debug);
									System.out.println("current function being made after replacing with function result:" + funBody);
									System.setOut(resultsOutput);
									token = LexAnalyzer.getToken(br);
									token_split = token.split(" ");
									System.setOut(debug);
									System.out.println("token value after completing replace value for function result" + token_split[0]);
									System.setOut(resultsOutput);
									if(!token_split[0].equals(")")){
										System.setOut(resultsOutput);
										System.out.println(token_split[0] + " Error, internal function failed to parse completely");
										System.setOut(console);
										System.out.println(token_split[0] + " Error, internal function failed to parse completely");
										System.exit(0);
									}
								}
								else{
									funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
									while(funBody.contains(" "+funParams.get(i)+" ")){
										funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
									}
									System.setOut(debug);
									System.out.println("current function being made after replacing in value:" + funBody);
									System.setOut(resultsOutput);
								}
								System.setOut(debug);
								System.out.println("current function being made:" + funBody);
								System.setOut(resultsOutput);
							}
							//parse new function
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
							System.setOut(debug);
							System.out.println("Function body made from peices in field exp:" + funBody);
							System.setOut(resultsOutput);
							Reader functionToParse = new StringReader(funBody);
							BufferedReader subParser = new BufferedReader(functionToParse);
							String subResult = "";
							token = LexAnalyzer.getToken(subParser);
							token_split = token.split(" ");
							if(token_split[0].equals("(")){
								subResult = eval(subParser,workingClass,workingClassName);
							}
							else{
								subResult = token_split[0];
							}
							System.setOut(debug);
							System.out.println("results of output:"+ subResult);
							System.setOut(resultsOutput);
							return subResult;
							/**
							else{
								System.setOut(resultsOutput);
								System.out.println(token_split[0] + " Error, field expresion parameters failed to read completely in field exp");
								System.setOut(console);
								System.out.println(token_split[0] + " Error, field expresion parameters failed to read completely in field exp");
								System.exit(0);
							}
							**/
						}
						else{
							System.setOut(resultsOutput);
							System.out.println(token_split[0] + " Error, couldn't find symbol during field expresion");
							System.setOut(console);
							System.out.println(token_split[0] + " Error, couldn't find symbol during field expresion");
							System.exit(0);
						}
					}
					
					else{
						//error
						System.setOut(resultsOutput);
						System.out.println(token_split[0] + " Error, expected token field expresion");
						System.setOut(console);
						System.out.println(token_split[0] + " Error, expected token field expresion");
						System.exit(0);
					}
				}
				else{
					//error
					System.setOut(resultsOutput);
					System.out.println(token_split[0] + " Error, start of class construction expected field expresion");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, start of class construction expected field expresion");
					System.exit(0);
				}
			}
			else if(token_split[0].equals(".")){
				String parametersOfMain = fieldExp(br,readToken,findClass,workingClassName);
				String classID = "";
				int indexOfClass = 0;
				while(parametersOfMain.charAt(indexOfClass) != ' '){
					classID += parametersOfMain.charAt(indexOfClass);
					indexOfClass++;
				}
				System.setOut(debug);
				System.out.println("class name found:"+classID);
				System.setOut(resultsOutput);
				multipleClassDef searcher = fullClassesTree;
				System.setOut(debug);
				while(!searcher.classInfo.className.equals(classID)){
					System.out.println("Current class name in dot finder:"+searcher.classInfo.className);
					searcher = searcher.multiclassdef;
				}
				System.setOut(resultsOutput);
				findClass = searcher;
				System.setOut(debug);
				System.out.println("Parameters of class in dot expression:"+parametersOfMain);
				System.out.println("functions of found class in dot:"+findClass.classInfo.funMap);
				System.setOut(resultsOutput);
				//first end paren
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				while(!token_split[1].equals("id")){
					System.setOut(debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					System.out.println("token being passed in field exp dot function:"+token_split[0]);
					System.setOut(resultsOutput);
					if(token_split[0].equals("-1")){
						System.setOut(resultsOutput);
						System.out.println(token_split[0] + " Error, couldn't find a symbol to parse in dot exp");
						System.setOut(console);
						System.out.println(token_split[0] + " Error, couldn't find a symbol to parse in dot exp");
						System.exit(0);
					}
				}
				//token
				if(token_split[1].equals("id")){
					if(parametersOfMain.contains(token_split[0])){
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
									System.setOut(debug);
									System.out.println("param val so far in dot sec of field in dot:" + parameterValue);
									System.setOut(resultsOutput);
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
					//token is a function
					else if(findClass.classInfo.funMap.containsKey(token_split[0])){
						//function was found, start getting its information
						int funParamLength = findClass.classInfo.funMap.get(token_split[0]).size();
						LinkedList<String> funParams = findClass.classInfo.funMap.get(token_split[0]);
						String funBody = findClass.classInfo.funBodyMap.get(token_split[0]);
						System.setOut(debug);
						System.out.println("Current function in dot:"+funBody);
						System.setOut(resultsOutput);
						//replace values section
						for(int i=0;i<funParamLength;i++){
							String result = "";
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
							System.setOut(debug);
							System.out.println("function part to replace in dot:"+funParams.get(i)+" with "+token_split[0]);
							System.setOut(resultsOutput);
							if(token_split[0].equals("(")){
								result = eval(br,findClass,parametersOfMain);
								funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
								while(funBody.contains(" "+funParams.get(i)+" ")){
									funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
								}
								System.setOut(debug);
								System.out.println("current function being made after replacing with function result in dot:" + funBody);
								System.setOut(resultsOutput);
								token = LexAnalyzer.getToken(br);
								token_split = token.split(" ");
								System.setOut(debug);
								System.out.println("token value after completing replace value for function result in dot" + token_split[0]);
								System.setOut(resultsOutput);
								if(!token_split[0].equals(")")){
									System.setOut(resultsOutput);
									System.out.println(token_split[0] + " Error, internal function failed to parse completely in dot");
									System.setOut(console);
									System.out.println(token_split[0] + " Error, internal function failed to parse completely in dot");
									System.exit(0);
								}
							}
							else{
								funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
								while(funBody.contains(" "+funParams.get(i)+" ")){
									funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
								}
								System.setOut(debug);
								System.out.println("current function being made after replacing in value in dot:" + funBody);
								System.setOut(resultsOutput);
							}
							System.setOut(debug);
							System.out.println("current function being made in dot:" + funBody);
							System.setOut(resultsOutput);
						}
						//parse new function
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
						if(token_split[0].equals(")")){
							System.setOut(debug);
							System.out.println("Function body made from peices in dot:" + funBody);
							System.setOut(resultsOutput);
							Reader functionToParse = new StringReader(funBody);
							BufferedReader subParser = new BufferedReader(functionToParse);
							String subResult = "";
							token = LexAnalyzer.getToken(subParser);
							token_split = token.split(" ");
							System.setOut(debug);
							System.out.println("token gotten at end of parse in dot:"+ token_split[0]);
							System.setOut(resultsOutput);
							if(token_split[0].equals("(")){
								subResult = eval(subParser,findClass,parametersOfMain);
							}
							else{
								subResult = token_split[0];
							}
							System.setOut(debug);
							System.out.println("results of output in dot:"+ subResult);
							System.setOut(resultsOutput);
							return subResult;
						}
						else{
							System.setOut(resultsOutput);
							System.out.println(token_split[0] + " Error, field expresion parameters failed to read completely in dot");
							System.setOut(console);
							System.out.println(token_split[0] + " Error, field expresion parameters failed to read completely in dot");
							System.exit(0);
						}
						
						
					}
					else{
						System.setOut(resultsOutput);
						System.out.println(token_split[0] + " Error, couldn't find symbol field expresion");
						System.setOut(console);
						System.out.println(token_split[0] + " Error, couldn't find symbol field expresion");
						System.exit(0);
					}
				}
				else{
					System.setOut(resultsOutput);
					System.out.println(token_split[0] + " Error, expected symbol name field expresion");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, expected symbol name field expresion");
					System.exit(0);
				}
			}
			else{
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected class name field expresion");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected class name field expresion");
				System.exit(0);
			}
		}
		/**
		else if(token_split[0].equals("this")){
			System.setOut(debug);
			System.out.println("got 'this' statement");
			System.setOut(resultsOutput);
			return parseThis(br,findClass,workingClassName);
		}
		**/
		else{
			//error
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, start of expression expected field expresion");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, start of expression expected field expresion");
			System.exit(0);
		}
		return "";
	}
	/**
	static String parseThis(BufferedReader br,  multipleClassDef findClass, String workingClassName) throws IOException{
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
			System.setOut(debug);
			System.out.println("main parameters:" + parametersOfMain);
			System.setOut(resultsOutput);
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
						System.setOut(debug);
						System.out.println("param val so far:" + parameterValue);
						System.setOut(resultsOutput);
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
			//function was found, start getting its information
			int funParamLength = findClass.classInfo.funMap.get(token_split[0]).size();
			LinkedList<String> funParams = findClass.classInfo.funMap.get(token_split[0]);
			String funBody = findClass.classInfo.funBodyMap.get(token_split[0]);
			System.setOut(debug);
			System.out.println("Current function in 'this':"+funBody);
			System.setOut(resultsOutput);
			//replace values section
			for(int i=0;i<funParamLength;i++){
				String result = "";
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				System.setOut(debug);
				System.out.println("function part to replace in 'this':"+funParams.get(i)+" with "+token_split[0]);
				System.setOut(resultsOutput);
				if(token_split[0].equals("(")){
					result = eval(br,findClass,workingClassName);
					funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
					while(funBody.contains(" "+funParams.get(i)+" ")){
						funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
					}
					System.setOut(debug);
					System.out.println("current function being made after replacing with function result in 'this':" + funBody);
					System.setOut(resultsOutput);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					System.setOut(debug);
					System.out.println("token value after completing replace value for function result in 'this'" + token_split[0]);
					System.setOut(resultsOutput);
					if(!token_split[0].equals(")")){
						System.setOut(resultsOutput);
						System.out.println(token_split[0] + " Error, internal function failed to parse completely in 'this'");
						System.setOut(console);
						System.out.println(token_split[0] + " Error, internal function failed to parse completely in 'this'");
						System.exit(0);
					}
				}
				else{
					funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
					while(funBody.contains(" "+funParams.get(i)+" ")){
						funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
					}
					System.setOut(debug);
					System.out.println("current function being made after replacing in value in 'this':" + funBody);
					System.setOut(resultsOutput);
				}
				System.setOut(debug);
				System.out.println("current function being made in 'this':" + funBody);
				System.setOut(resultsOutput);
			}
			//parse new function
			System.setOut(debug);
			System.out.println("token after parameter parse before calling next in 'this':" + token_split[0]);
			System.setOut(resultsOutput);
			
			//token = LexAnalyzer.getToken(br);
			//token_split = token.split(" ");

			System.setOut(debug);
			System.out.println("token after parameter parse after calling next in 'this':" + token_split[0]);
			System.setOut(resultsOutput);
			if(token_split[0].equals(")")){
				System.setOut(debug);
				System.out.println("Function body made from peices in 'this':" + funBody);
				System.setOut(resultsOutput);
				Reader functionToParse = new StringReader(funBody);
				BufferedReader subParser = new BufferedReader(functionToParse);
				String subResult = "";
				token = LexAnalyzer.getToken(subParser);
				token_split = token.split(" ");
				if(token_split[0].equals("(")){
					subResult = eval(subParser,findClass,workingClassName);
				}
				else{
					subResult = token_split[0];
				}
				System.setOut(debug);
				System.out.println("results of output in 'this':"+ subResult);
				System.setOut(resultsOutput);
				return subResult;
			}
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals(")")){
				System.setOut(debug);
				System.out.println("Function body made from peices in 'this':" + funBody);
				System.setOut(resultsOutput);
				Reader functionToParse = new StringReader(funBody);
				BufferedReader subParser = new BufferedReader(functionToParse);
				String subResult = "";
				token = LexAnalyzer.getToken(subParser);
				token_split = token.split(" ");
				if(token_split[0].equals("(")){
					subResult = eval(subParser,findClass,workingClassName);
				}
				else{
					subResult = token_split[0];
				}
				System.setOut(debug);
				System.out.println("results of output in 'this':"+ subResult);
				System.setOut(resultsOutput);
				return subResult;
			}
			else{
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, field expresion parameters failed to read completely in 'this'");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, field expresion parameters failed to read completely in 'this'");
				System.exit(0);
			}
		}
		else{
			//error
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, failed to parse 'this' expresion");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, failed to parse 'this' expresion");
			System.exit(0);
		}
		return "error";
	}
	**/
}




























