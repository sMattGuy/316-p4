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
	static int sequenceNumber = 0;
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
		System.setOut(debug);
		System.out.println(sequenceNumber + " current token being parsed:" + token_split[0]);
		System.setOut(resultsOutput);
		//begin parse
		//go to expression function
		//arith
		if(token_split[0].equals("+") || token_split[0].equals("*") || token_split[0].equals("-") || token_split[0].equals("/")){
			System.setOut(debug);
			sequenceNumber++;
			System.out.println(sequenceNumber + " Started evaluating arith expression");
			System.setOut(resultsOutput);
			double result = arithExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(sequenceNumber + " returned from evaluating arith expression with:" + result);
			System.setOut(resultsOutput);
			token = LexAnalyzer.getToken(brExp);
			token_split = token.split(" ");
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expression arith did not fully parse");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expression arith did not fully parse");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(brExp);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
			System.setOut(debug);
			System.out.println(sequenceNumber + " arith expression ended with close paren");
			System.setOut(resultsOutput);
			if(isDouble){
				isDouble = false;
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
			System.setOut(debug);
			sequenceNumber++;
			System.out.println(sequenceNumber + " Started evaluating boolean expression");
			System.setOut(resultsOutput);
			boolean results = boolExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(sequenceNumber + " returned from evaluating boolean expression with:" + results);
			System.setOut(resultsOutput);
			token = LexAnalyzer.getToken(brExp);
			token_split = token.split(" ");
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expression boolean did not fully parse");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expression boolean did not fully parse");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(brExp);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
			System.setOut(debug);
			System.out.println("boolean ended correctly");
			System.setOut(resultsOutput);
			return Boolean.toString(results);
		}
		//comparison
		else if(token_split[0].equals("<") || token_split[0].equals(">") || token_split[0].equals("<=") || token_split[0].equals(">=") || token_split[0].equals("=")){
			System.setOut(debug);
			sequenceNumber++;
			System.out.println(sequenceNumber + " Started evaluating comp expression");
			System.setOut(resultsOutput);
			boolean results = compExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(sequenceNumber + " returned from evaluating comp expression with:" + results);
			System.setOut(resultsOutput);
			token = LexAnalyzer.getToken(brExp);
			token_split = token.split(" ");
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expression comparision did not fully parse");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expression comparision did not fully parse");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(brExp);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
			System.setOut(debug);
			System.out.println("comp ended correctly");
			System.out.println(results);
			System.setOut(resultsOutput);
			return Boolean.toString(results);
		}
		//condition
		else if(token_split[0].equals("if")){
			System.setOut(debug);
			sequenceNumber++;
			System.out.println(sequenceNumber + " Started evaluating cond expression");
			System.setOut(resultsOutput);
			String results = condExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(sequenceNumber + " returned from evaluating cond expression with:" + results);
			System.setOut(resultsOutput);
			token = LexAnalyzer.getToken(brExp);
			token_split = token.split(" ");
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expression condition did not fully parse");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expression condition did not fully parse");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(brExp);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
			System.setOut(debug);
			System.out.println("cond ended correctly");
			System.out.println(results);
			System.setOut(resultsOutput);
			return results;
		}
		//constructor
		else if(token_split[1].equals("id")){
			System.setOut(debug);
			sequenceNumber++;
			System.out.println(sequenceNumber + " Started evaluating const expression");
			System.setOut(resultsOutput);
			String results = constExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(sequenceNumber + " returned from const field expression with:" + results);
			System.setOut(resultsOutput);
			if(results.equals("referrence")){
				System.setOut(debug);
				System.out.println(sequenceNumber + " returning without advancing token");
				System.setOut(resultsOutput);
				return results;
			}
			token = LexAnalyzer.getToken(brExp);
			token_split = token.split(" ");
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expression constructor did not fully parse");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expression constructor did not fully parse");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(brExp);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
			System.setOut(debug);
			System.out.println(sequenceNumber + " const ended correctly");
			System.out.println(results);
			System.setOut(resultsOutput);
			return results;
		}
		//field getter
		else if(token_split[0].equals(".")){
			System.setOut(debug);
			sequenceNumber++;
			System.out.println(sequenceNumber + " Started evaluating field expression");
			System.setOut(resultsOutput);
			String results = fieldExp(brExp,token_split[0],completeParse,workingClassName);
			System.setOut(debug);
			System.out.println(sequenceNumber + " returned from evaluating field expression with:" + results);
			System.setOut(resultsOutput);
			token = LexAnalyzer.getToken(brExp);
			token_split = token.split(" ");
			if(!token_split[0].equals(")")){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expression fields did not fully parse");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expression fields did not fully parse");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(brExp);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
			System.setOut(debug);
			System.out.println(sequenceNumber + " field ended correctly");
			System.out.println(results);
			System.setOut(resultsOutput);
			return results;
		}
		//error
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, couldn't find valid parse for expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, couldn't find valid parse for expression");
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(brExp);
				token_split = token.split(" ");
			}
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
		System.setOut(debug);
		System.out.println(sequenceNumber + " arithmatic start first token:"+token_split[0]);
		System.setOut(resultsOutput);
		if(token_split[0].equals("(")){
			//first unit is new expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " arithmatic token if first token is a expression:"+token_split[0]);
			System.setOut(resultsOutput);
			try{
				unit1 = Double.parseDouble(eval(br,findClass,workingClassName));
				if(unit1 % 1 != 0){
					isDouble = true;
				}
				System.setOut(debug);
				System.out.println(sequenceNumber + " arithmatic unit1 returned from eval");
				System.setOut(resultsOutput);
			}
			catch(Exception e){
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression for unit1");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression for unit1");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
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
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit1");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit1");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(token_split[0].equals(")")){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
		}
		System.setOut(debug);
		System.out.println(sequenceNumber + " arithmatic start second token:"+token_split[0]);
		System.setOut(resultsOutput);
		//gets second token
		if(token_split[0].equals("(")){
			//second unit is new expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " arithmatic being parsed expression");
			System.setOut(resultsOutput);
			try{
				unit2 = Double.parseDouble(eval(br,findClass,workingClassName));
				if(unit2 % 1 != 0){
					isDouble = true;
				}
				System.setOut(debug);
				System.out.println(sequenceNumber + " arithmatic unit2 returned from eval");
				System.setOut(resultsOutput);
				
			}
			catch(Exception e){
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression for unit2");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected arithmatic expression during arithmatic expression for unit2");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
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
			}
			catch(Exception e){
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit2");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during arithmatic expression for unit2");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		if(readToken.equals("+")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " add:" + (unit1) + " " +(unit2) + " " + (unit1 + unit2));
			System.setOut(resultsOutput);
			return unit1 + unit2;
		}
		else if(readToken.equals("*")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " times:" + (unit1) + " " +(unit2) + " " + (unit1 * unit2));
			System.setOut(resultsOutput);
			return unit1 * unit2;
		}
		else if(readToken.equals("/")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " divide:" + (unit1) + " " +(unit2) + " " + (unit1 / unit2));
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
			System.out.println(sequenceNumber + " minus:" + (unit1) + " " +(unit2) + " " + (unit1 - unit2));
			System.setOut(resultsOutput);
			return unit1 - unit2;
		}
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected result to return during arithmatic expression");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during arithmatic expression");
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		return -999999;
	}
	
	static boolean boolExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get boolean type
		boolean unit1 = false;
		boolean unit2 = false;
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " boolean being parsed expression");
			System.setOut(resultsOutput);
			String boolSubResult = eval(br,findClass,workingClassName);
			if(boolSubResult.equals("true") || boolSubResult.equals("false")){
				unit1 = Boolean.parseBoolean(boolSubResult);
				System.setOut(debug);
				System.out.println(sequenceNumber + " boolean unit1 returned from eval");
				System.setOut(resultsOutput);
				
			}
			else{
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected result to return during boolean expression unit1");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected result to return during boolean expression unit1");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		else{
			//first unit is boolean
			if(token_split[0].equals("true") || token_split[0].equals("false")){
				unit1 = Boolean.parseBoolean(token_split[0]);
			}
			else{
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		System.setOut(debug);
		System.out.println(sequenceNumber + " unit1 has completely parsed");
		System.setOut(resultsOutput);
		//this will be an issue soon i can feel it
		if(readToken.equals("!")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " recieved not operation turning:" + unit1);
			unit1 = !unit1;
			System.out.println(sequenceNumber + " into:" + unit1);
			System.setOut(resultsOutput);
			return unit1;
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(token_split[0].equals(")")){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
		}
		System.setOut(debug);
		System.out.println(sequenceNumber + " token after unit1; next token:" + token_split[0]);
		System.setOut(resultsOutput);
		if(token_split[0].equals("(")){
			//second unit is new expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " boolean being parsed expression");
			System.setOut(resultsOutput);
			String boolSubResult = eval(br,findClass,workingClassName);
			if(boolSubResult.equals("true") || boolSubResult.equals("false")){
				unit2 = Boolean.parseBoolean(boolSubResult);
				System.setOut(debug);
				System.out.println(sequenceNumber + " boolean unit2 returned from eval\ntoken pulled after unit2 eval:"+token_split[0]);
				System.setOut(resultsOutput);
				
			}
			else{
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected result to return during boolean expression for unit2");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected result to return during boolean expression for unit2");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		else{
			//second unit is boolean value
			if(token_split[0].equals("true") || token_split[0].equals("false")){
				unit2 = Boolean.parseBoolean(token_split[0]);
			}
			else{
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression unit 2");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean during boolean expression unit 2");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		System.setOut(debug);
		System.out.println(sequenceNumber + " second token has been pasred:" + unit2);
		System.setOut(resultsOutput);
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
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		System.setOut(debug);
		System.out.println(sequenceNumber + " shouldn't be able to get here:" + token_split[0]);
		System.setOut(resultsOutput);
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
		
		boolean unit1Boolean = false;
		boolean unit2Boolean = false;
		boolean unit1Double = false;
		boolean unit2Double = false;
		boolean unit1Object = false;
		boolean unit2Object = false;

		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		System.setOut(debug);
		System.out.println(sequenceNumber + " first token from comp expression:"+token_split[0]);
		System.setOut(resultsOutput);
		if(token_split[0].equals("(")){
			//first comparision is an expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " comparision being parsed expression");
			System.setOut(resultsOutput);
			String evalResult = eval(br,findClass,workingClassName);
			try{
				unit1 = Double.parseDouble(evalResult);
				unit1Double = true;
				System.setOut(debug);
				System.out.println(sequenceNumber + " comp unit1 returned from eval as arithmatic");
				System.setOut(resultsOutput);
			}
			catch(Exception e){
				//try next parse type
				if(evalResult.equals("true") || evalResult.equals("false")){
					unit1b = Boolean.parseBoolean(evalResult);
					unit1Boolean = true;
					System.setOut(debug);
					System.out.println(sequenceNumber + " comp unit1 returned from eval as boolean");
					System.setOut(resultsOutput);
				}
				else{
					unit1Obj = evalResult;
					unit1Object = true;
					System.setOut(debug);
					System.out.println(sequenceNumber + " comp unit1 returned from eval as object");
					System.setOut(resultsOutput);
				}
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
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		//gets next token
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		System.setOut(debug);
		System.out.println(sequenceNumber + " second token from comp expression:"+token_split[0]);
		System.setOut(resultsOutput);
		if(token_split[0].equals("(")){
			//second unit is new expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " comparision being parsed expression");
			System.setOut(resultsOutput);
			String evalResult = eval(br,findClass,workingClassName);
			try{
				unit2 = Double.parseDouble(evalResult);
				unit2Double = true;
				System.setOut(debug);
				System.out.println(sequenceNumber + " comp unit2 returned from eval as arithmatic\ntoken pulled after unit2 eval:"+token_split[0]);
				System.setOut(resultsOutput);
			}
			catch(Exception e){
				//try next parse type
				if(evalResult.equals("true") || evalResult.equals("false")){
					unit2b = Boolean.parseBoolean(evalResult);
					unit2Boolean = true;
					System.setOut(debug);
					System.out.println(sequenceNumber + " comp unit2 returned from eval as boolean:"+unit2b+"\ntoken pulled after unit2 eval:"+token_split[0]);
					System.setOut(resultsOutput);
				}
				else{
					unit2Obj = evalResult;
					unit2Object = true;
					System.setOut(debug);
					System.out.println(sequenceNumber + " comp unit2 returned from eval as object\ntoken pulled after unit2 eval:"+token_split[0]);
					System.setOut(resultsOutput);
				}
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
				System.out.println(token_split[0] + " Error, expected number during comparison expression unit2");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected number during comparison expression unit2");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		if(readToken.equals("<")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " <:" + (unit1) + " " +(unit2) + " " + (unit1 < unit2));
			System.setOut(resultsOutput);
			return unit1 < unit2;
		}
		else if(readToken.equals(">")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " >:" + (unit1) + " " +(unit2) + " " + (unit1 > unit2));
			System.setOut(resultsOutput);
			return unit1 > unit2;
		}
		else if(readToken.equals("<=")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " <=:" + (unit1) + " " +(unit2) + " " + (unit1 <= unit2));
			System.setOut(resultsOutput);
			return unit1 <= unit2;
		}
		else if(readToken.equals(">=")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " >=:" + (unit1) + " " +(unit2) + " " + (unit1 >= unit2));
			System.setOut(resultsOutput);
			return unit1 >= unit2;
		}
		else if(readToken.equals("=")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " equal:" + (unit1) + " " +(unit2) + " " + (unit1 == unit2));
			System.out.println(sequenceNumber + "\nunit1bool:"+unit1Boolean+"\nunit2bool:"+unit2Boolean+"\nunit1Double:"+unit1Double+"\nunit2Double:"+unit2Double+"\nunit1Object:"+unit1Object+"\nunit2Object:"+unit2Object);
			System.setOut(resultsOutput);
			if(unit1Boolean && unit2Boolean){
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
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		return false;
	}
	
	static String condExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		//get arthimatic type
		String unit1 = "";
		String unit2 = "";
		boolean unit1b = false;
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			//first unit is new expression
			System.setOut(debug);
			System.out.println(sequenceNumber + " cond boolean being parsed expression");
			System.setOut(resultsOutput);
			String condSubResult = "";
			condSubResult = eval(br,findClass,workingClassName);
			if(condSubResult.equals("true") || condSubResult.equals("false")){
				unit1b = Boolean.parseBoolean(condSubResult);
				System.setOut(debug);
				System.out.println(sequenceNumber + " cond unit1b returned from eval as boolean\nunit1b eval to :"+unit1b);
				System.setOut(resultsOutput);
				System.setOut(debug);
			}
			else{
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		else{
			//comp expected
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected boolean expression during conditional check");
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		token = LexAnalyzer.getToken(br);
		token_split = token.split(" ");
		if(token_split[0].equals(")")){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");	
		}
		//get first number for true statement 
		if(token_split[0].equals("(")){
			System.setOut(debug);
			System.out.println(sequenceNumber + " Parsing in cond expression as unit1");
			System.setOut(resultsOutput);
			unit1 = eval(br,findClass,workingClassName);
		}
		else{
			System.setOut(debug);
			System.out.println(sequenceNumber + " cond expression for unit1 is value:"+token_split[0]);
			System.setOut(resultsOutput);
			unit1 = token_split[0];
		}
		//check if first statement is true, if so simply skip to end
		System.setOut(debug);
		System.out.println(sequenceNumber + " is statement true:"+unit1b + "\nto return if true:"+unit1);
		System.setOut(resultsOutput);
		if(unit1b){
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals(")")){
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");	
			}
			if(token_split[0].equals("(")){
				int parenCount = 1;
				while(parenCount != 0){
					System.setOut(debug);
					System.out.print(token_split[0] + " ");
					System.setOut(resultsOutput);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					if(token_split[0].equals("(")){
						parenCount++;
					}
					else if(token_split[0].equals(")")){
						parenCount--;
					}
					if(parenCount == 0){
						break;
					}
				}
			}
			return unit1;
		}
		else{
			//get second number for false statement
			System.setOut(debug);
			System.out.println(sequenceNumber + " token in unit 2 before split:"+token_split[0]);
			System.setOut(resultsOutput);
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals(")")){
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.setOut(debug);
			System.out.println(sequenceNumber + " token in unit 2 after split:"+token_split[0]);
			System.setOut(resultsOutput);
			//start of function
			if(token_split[0].equals("(")){
				System.setOut(debug);
				System.out.println(sequenceNumber + " Parsing in cond expression as unit2");
				System.setOut(resultsOutput);
				unit2 = eval(br,findClass,workingClassName);
			}
			else{
				unit2 = token_split[0];
			}
		}
		System.setOut(debug);
		System.out.println(sequenceNumber + " Value of unit1 in cond:"+unit1);
		System.out.println(sequenceNumber + " Value of unit2 in cond:"+unit2);
		System.setOut(resultsOutput);
		if(!unit1b){
			return unit2;
		}
		else{
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, expected result to return during conditional check");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, expected result to return during conditional check");
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		return "";
	}

	static String constExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		boolean foundClass = false;
		String builtObject = "";
		String token = "";
		String[] token_split = {"",""};
		multipleClassDef searcher = fullClassesTree;
		while(searcher.multiclassdef != null){
			if(searcher.classInfo.className.equals(readToken)){
				System.setOut(debug);
				System.out.println(sequenceNumber + " constructor found class id");
				System.setOut(resultsOutput);
				foundClass = true;
				findClass = searcher;
				builtObject = findClass.classInfo.className + " object: {";
				System.setOut(debug);
				System.out.println(sequenceNumber + " class found:"+findClass.classInfo.className + "\nclass has parameters:"+findClass.classInfo.fields);
				System.setOut(resultsOutput);
				boolean firstPass = true;
				for(int i=0;i<findClass.classInfo.fields.size();i++){
					if(i!=0){
						builtObject += ", ";
					}
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					if(token_split[0].equals(")") && firstPass){
						System.setOut(debug);
						System.out.println(sequenceNumber + " returning referrence constructor");
						System.setOut(resultsOutput);
						return "referrence";
					}
					if(token_split[0].equals(")")){
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");	
					}
					if(token_split[0].equals("(")){
						System.setOut(debug);
						System.out.println(sequenceNumber + " constructor contains expression");
						System.setOut(resultsOutput);
						String constInnerExpResult = eval(br,findClass,workingClassName);
						builtObject += findClass.classInfo.fields.get(i) + "=" + constInnerExpResult;
						System.setOut(debug);
						System.out.println(sequenceNumber + " constructor returned from evaluation with:"+constInnerExpResult);
						System.setOut(resultsOutput);
					}
					else{
						builtObject += findClass.classInfo.fields.get(i) + "=" + token_split[0];
					}
					firstPass = false;
					System.setOut(debug);
					System.out.println(sequenceNumber + " constructor built object progress:"+builtObject);
					System.setOut(resultsOutput);
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
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		return "";
	}
	//project 4 modified for user functions 
	static String fieldExp(BufferedReader br,  String readToken, multipleClassDef findClass, String workingClassName) throws IOException{
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		if(token_split[0].equals("(")){
			String classInfo = eval(br,findClass,workingClassName);
			if(!classInfo.equals("referrence")){
				System.setOut(debug);
				System.out.println(sequenceNumber + " new class wasn't refference");
				System.setOut(resultsOutput);
				boolean hasParams = false;
				int braceCount = 0;
				for(int i=0;i<classInfo.length();i++){
					char temp = classInfo.charAt(i);
					if(temp == '{'){
							temp = classInfo.charAt(i+1);
							if(temp == '}'){
								break;
							}
						hasParams = true;
						break;
					}
				}
				if(hasParams){
					System.setOut(debug);
					System.out.println(sequenceNumber + " new class has parameters, replacing the working class");
					System.setOut(resultsOutput);
					workingClassName = classInfo;
				}
				else{
					System.setOut(debug);
					System.out.println(sequenceNumber + " new class doesnt have new parameters, keeping old working class");
					System.setOut(resultsOutput);
				}
			}
			else{
				if(workingClassName == null){
					System.setOut(resultsOutput);
					System.out.println(token_split[0] + " Error, tried referencing an uninitialized class in field exp");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, tried referencing an uninitialized class in field exp");
					while(!token_split[0].equals("-1")){
						System.out.print(token_split[0] + " ");
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
					}
					System.exit(0);
				}
				System.setOut(debug);
				System.out.println(sequenceNumber + " reference was returned:"+workingClassName);
				classInfo = workingClassName;
				System.setOut(resultsOutput);
			}
			String classID = "";
			int index = 0;
			while(classInfo.charAt(index)!=' '){
				classID += classInfo.charAt(index);
				index++;
			}
			System.setOut(debug);
			System.out.println(sequenceNumber + " class gotten:" + classInfo + "\nname parsed:" + classID);
			System.setOut(resultsOutput);
			multipleClassDef workingClass = fullClassesTree;
			while(!workingClass.classInfo.className.equals(classID)){
				System.setOut(debug);
				System.out.println(sequenceNumber + " current searching class in field exp:" + workingClass.classInfo.className);
				System.setOut(resultsOutput);
				workingClass = workingClass.multiclassdef;
			}
			System.setOut(debug);
			System.out.println(sequenceNumber + " class found");
			System.setOut(resultsOutput);
			//get token after constructor;
			token = LexAnalyzer.getToken(br);
			token_split = token.split(" ");
			if(token_split[0].equals(")")){
				System.setOut(debug);
				System.out.println(sequenceNumber + " skipping close paren");
				System.setOut(resultsOutput);
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");	
			}
			if(token_split[0].equals("(") || token_split[1].equals("id")){
				if(!token_split[1].equals("id")){
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				if(token_split[1].equals("id")){
					//gotten function/variable to return
					//if parameter gotten is in fields
					System.setOut(debug);
					System.out.println(sequenceNumber + " token id in field:" + token_split[0]);
					System.out.println(sequenceNumber + " function map of class:" + workingClass.classInfo.funMap);
					System.out.println(sequenceNumber + " does token id exist in function:" + workingClass.classInfo.funMap.containsKey(token_split[0]));
					System.setOut(resultsOutput);
					if(classInfo.contains(token_split[0])){
						System.setOut(debug);
						System.out.println(sequenceNumber + " class info contains:" + token_split[0]);
						System.setOut(resultsOutput);
						String parametersOfMain = "";
						int braceCount = 0;
						for(int i=0;i<classInfo.length();i++){
							char temp = classInfo.charAt(i);
							if(temp == '{'){
								System.setOut(debug);
								System.out.println(sequenceNumber + " new field found");
								System.setOut(resultsOutput);
								braceCount++;
								do{
									i++;
									temp = classInfo.charAt(i);
									System.setOut(debug);
									System.out.println(sequenceNumber + " temp being build in parameters:" + temp);
									System.setOut(resultsOutput);
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
									System.setOut(debug);
									System.out.println(sequenceNumber + " main parameters so far:" + parametersOfMain);
									System.setOut(resultsOutput);
								}while(braceCount != 0);
							}
						}
						System.setOut(debug);
						System.out.println(sequenceNumber + " main parameters:" + parametersOfMain);
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
									System.out.println(sequenceNumber + " param val so far:" + parameterValue);
									System.setOut(resultsOutput);
									i++;
									if(i == parametersOfMain.length()){
										break;
									}
								}
								if(parameterName.equals(token_split[0])){
									System.setOut(debug);
									System.out.println(sequenceNumber + " parameter value " + parameterName + " equals token:" + parameterValue);
									System.setOut(resultsOutput);
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
						System.out.println(sequenceNumber + " Current function:"+funBody);
						System.setOut(resultsOutput);
						while(funBody.contains(" this ")){
							funBody = funBody.replaceAll(" this ","  ("+workingClass.classInfo.className+") ");
							System.setOut(debug);
							System.out.println(sequenceNumber + " current replacement of this:" + funBody);
							System.setOut(resultsOutput);
						}
						//replace values section
						boolean instantFunc = false;
						for(int i=0;i<funParamLength;i++){
							String result = "";
							if(!instantFunc){
								token = LexAnalyzer.getToken(br);
								token_split = token.split(" ");
							}
							System.setOut(debug);
							System.out.println(sequenceNumber + " function part to replace:"+funParams.get(i)+" with "+token_split[0]);
							if(funParams.get(i).equals(token_split[0])){
								System.setOut(resultsOutput);
								System.out.println(funParams.get(i) + " Error, couldn't find symbol value");
								System.setOut(console);
								System.out.println(funParams.get(i) + " Error, couldn't find symbol value");
								while(!token_split[0].equals("-1")){
									System.out.print(token_split[0] + " ");
									token = LexAnalyzer.getToken(br);
									token_split = token.split(" ");
								}
								System.exit(0);
							}
							System.setOut(resultsOutput);
							if(token_split[0].equals("(")){
								System.setOut(debug);
								System.out.println(sequenceNumber + " function part to replace is expression");
								System.setOut(resultsOutput);
								result = eval(br,workingClass,workingClassName);
								System.setOut(debug);
								System.out.println(sequenceNumber + " result evaluation returned with:" + result);
								System.setOut(resultsOutput);
								funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
								while(funBody.contains(" "+funParams.get(i)+" ")){
									funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+result+" ");
								}
								System.setOut(debug);
								System.out.println(sequenceNumber + " current function being made after replacing with function result in field:" + funBody);
								System.setOut(resultsOutput);
								token = LexAnalyzer.getToken(br);
								token_split = token.split(" ");
								if(token_split[0].equals("(")){
									instantFunc = true;
								}
								System.setOut(debug);
								System.out.println(sequenceNumber + " token value after completing replace value for function result in field:" + token_split[0]);
								System.setOut(resultsOutput);	
								if(!token_split[0].equals(")") && !instantFunc){
									System.setOut(resultsOutput);
									System.out.println(token_split[0] + " Error, internal function failed to parse completely in field exp");
									System.setOut(console);
									System.out.println(token_split[0] + " Error, internal function failed to parse completely in field exp");
									while(!token_split[0].equals("-1")){
										System.out.print(token_split[0] + " ");
										token = LexAnalyzer.getToken(br);
										token_split = token.split(" ");
									}
									System.exit(0);
								}
							}
							else{
								funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
								while(funBody.contains(" "+funParams.get(i)+" ")){
									funBody = funBody.replaceAll(" "+funParams.get(i)+" ", " "+token_split[0]+" ");
								}
								System.setOut(debug);
								System.out.println(sequenceNumber + " current function being made after replacing in value:" + funBody);
								System.setOut(resultsOutput);
							}
							System.setOut(debug);
							System.out.println(sequenceNumber + " current function being made:" + funBody);
							System.setOut(resultsOutput);
						}
						//parse new function
						System.setOut(debug);
						System.out.println(sequenceNumber + " Function body made from peices in field exp:" + funBody);
						System.setOut(resultsOutput);
						Reader functionToParse = new StringReader(funBody);
						BufferedReader subParser = new BufferedReader(functionToParse);
						String subResult = "";
						token = LexAnalyzer.getToken(subParser);
						token_split = token.split(" ");
						if(token_split[0].equals("(")){
							System.setOut(debug);
							System.out.println(sequenceNumber + " result in field exp is expression:" + funBody);
							System.setOut(resultsOutput);
							subResult = eval(subParser,workingClass,workingClassName);
						}
						else{
							System.setOut(debug);
							System.out.println(sequenceNumber + " result in field exp is single value:" + funBody);
							System.setOut(resultsOutput);
							subResult = token_split[0];
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
						}
						System.setOut(debug);
						System.out.println(sequenceNumber + " results of output in field exp:" + funBody + " resulted in " + subResult);
						System.setOut(resultsOutput);
						if(subResult.equals("referrence")){
							return classInfo;
						}
						else{
							return subResult;
						}
					}
					else{
						System.setOut(resultsOutput);
						System.out.println(token_split[0] + " Error, couldn't find symbol during field expresion");
						System.setOut(console);
						System.out.println(token_split[0] + " Error, couldn't find symbol during field expresion");
						while(!token_split[0].equals("-1")){
							System.out.print(token_split[0] + " ");
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
						}
						System.exit(0);
					}
				}
				else{
					//error
					System.setOut(resultsOutput);
					System.out.println(token_split[0] + " Error, expected token field expresion");
					System.setOut(console);
					System.out.println(token_split[0] + " Error, expected token field expresion");
					while(!token_split[0].equals("-1")){
						System.out.print(token_split[0] + " ");
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
					}
					System.exit(0);
				}
			}
			else{
				//error
				System.setOut(resultsOutput);
				System.out.println(token_split[0] + " Error, start of class construction expected field expresion");
				System.setOut(console);
				System.out.println(token_split[0] + " Error, start of class construction expected field expresion");
				while(!token_split[0].equals("-1")){
					System.out.print(token_split[0] + " ");
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
				}
				System.exit(0);
			}
		}
		else{
			//error
			System.setOut(resultsOutput);
			System.out.println(token_split[0] + " Error, start of expression expected field expresion");
			System.setOut(console);
			System.out.println(token_split[0] + " Error, start of expression expected field expresion");
			while(!token_split[0].equals("-1")){
				System.out.print(token_split[0] + " ");
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
			}
			System.exit(0);
		}
		return "";
	}
}