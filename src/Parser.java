import java.io.*;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Comparator;

public class Parser{
	//recursive function for solving the expressions
	static String parseExpression(String token, String[] token_split, exp expression, BufferedReader br, LinkedList params, LinkedList fields, String functionBody, PrintStream classOutput, PrintStream debug) throws IOException{
		functionBody = functionBody.concat(token_split[0] + " ");
		System.setOut(debug);
		System.out.println("Parsing Token: " + token);
		System.setOut(classOutput);
		if(token_split[1].equals("id")){
			if(params.contains(token_split[0]) || fields.contains(token_split[0])){
				expression.id = token_split[0];
			}
			else{
				System.setOut(classOutput);
				System.out.println(token_split[0] + " Error, variable not delcared in scope");
				System.exit(0);
			}
			return functionBody;
		}
		else if(token_split[1].equals("int")){
			expression.integer = token_split[0];
			return functionBody;
		}
		else if(token_split[1].equals("float") || token_split[1].equals("floatE")){
			expression.decimal = token_split[0];
			return functionBody;
		}
		else if(token_split[1].equals("keyword_null") || token_split[1].equals("keyword_this")){
			expression.keyword = token_split[0];
			return functionBody;
		}
		else if(token_split[0].equals("(")){
			System.setOut(debug);
			System.out.println("Parsing function expression: " + token);
			System.setOut(classOutput);
			//start of fun exp
			while(!token_split[0].equals(")")){
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				System.setOut(debug);
				System.out.println("Function expression internals: " + token);
				System.setOut(classOutput);
				//fun call
				if(token_split[1].equals("id")){
					//fun name
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.funcall = new funCall();
					expression.funexp.funcall.funname = new funName();
					expression.funexp.funcall.funname.id = token_split[0];
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					//fun call exp list
					expression.funexp.funcall.multiexplist = new multiExpList();
					expression.funexp.funcall.multiexplist.expression = new exp();
					multiExpList workingExp = expression.funexp.funcall.multiexplist;
					//while internal function is not complete
					while(!token_split[0].equals(")")){
						functionBody = parseExpression(token,token_split,workingExp.expression,br,params,fields,functionBody,classOutput,debug);
						workingExp.multiexplist = new multiExpList();
						workingExp = workingExp.multiexplist;
						workingExp.expression = new exp();
						//next internal token
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
						System.setOut(debug);
						System.out.println("Next internal token: " + token);
						System.setOut(classOutput);
					}
					System.setOut(debug);
					System.out.println("Function expression complete");
					System.setOut(classOutput);
				}
				//bin exp -> arith
				else if(token_split[1].equals("add") || token_split[1].equals("sub") || token_split[1].equals("mul") || token_split[1].equals("div")){
					//bin exp -> arith -> arith op
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.binexp = new binaryExp();
					expression.funexp.binexp.arithexp = new arithExp();
					expression.funexp.binexp.arithexp.arithop = new arithOp();
					expression.funexp.binexp.arithexp.arithop.operator = token_split[0];
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.arithexp.exp1 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.arithexp.exp1,br,params,fields,functionBody,classOutput,debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.arithexp.exp2 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.arithexp.exp2,br,params,fields,functionBody,classOutput,debug);
				}
				//bin exp -> bool
				else if(token_split[1].equals("or") || token_split[1].equals("and")){
					//bin exp -> bool -> bool op
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.binexp = new binaryExp();
					expression.funexp.binexp.boolexp = new boolExp();
					expression.funexp.binexp.boolexp.boolop = new boolOp();
					expression.funexp.binexp.boolexp.boolop.operator = token_split[0];
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.boolexp.exp1 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.boolexp.exp1,br,params,fields,functionBody,classOutput,debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.boolexp.exp2 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.boolexp.exp2,br,params,fields,functionBody,classOutput,debug);
				}
				//bin exp -> comp
				else if(token_split[1].equals("gt") || token_split[1].equals("ge") || token_split[1].equals("lt") || token_split[1].equals("le") || token_split[1].equals("eq")){
					//bin exp -> comp -> comp op
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.binexp = new binaryExp();
					expression.funexp.binexp.compexp = new compExp();
					expression.funexp.binexp.compexp.compop = new compOp();
					expression.funexp.binexp.compexp.compop.operator = token_split[0];
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.compexp.exp1 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.compexp.exp1,br,params,fields,functionBody,classOutput,debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.compexp.exp2 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.compexp.exp2,br,params,fields,functionBody,classOutput,debug);
				}
				//bin exp -> dot
				else if(token_split[1].equals("dotOp")){
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.binexp = new binaryExp();
					expression.funexp.binexp.dotexp = new dotExp();
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.dotexp.exp1 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.dotexp.exp1,br,params,fields,functionBody,classOutput,debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.binexp.dotexp.exp2 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.binexp.dotexp.exp2,br,params,fields,functionBody,classOutput,debug);
				}
				//cond
				else if(token_split[1].equals("keyword_if")){
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.condition = new cond();
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.condition.exp1 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.condition.exp1,br,params,fields,functionBody,classOutput,debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.condition.exp2 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.condition.exp2,br,params,fields,functionBody,classOutput,debug);
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.condition.exp3 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.condition.exp3,br,params,fields,functionBody,classOutput,debug);
				}
				//not
				else if(token_split[1].equals("not")){
					functionBody = functionBody.concat(token_split[0] + " ");
					expression.funexp = new funExp();
					expression.funexp.notType = new not();
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					expression.funexp.notType.exp1 = new exp();
					functionBody = parseExpression(token,token_split,expression.funexp.notType.exp1,br,params,fields,functionBody,classOutput,debug);
				}
				else if(token_split[0].equals(")")){
					functionBody = functionBody.concat(token_split[0] + " ");
					continue;
				}
				else{
					System.setOut(classOutput);
					System.out.println(token_split[0] + " Syntax Error, token not valid in any context of function expression");
					System.exit(0);
				}
			}
		}
		return functionBody;
	}
	
	//stand alone function
	static multipleClassDef getParse(BufferedReader br, PrintStream classOutput, PrintStream debug) throws IOException{
		//parse tree classes
		multipleClassDef completeParse = new multipleClassDef();
		completeParse.classInfo = new ClassDefEntry();
		//individual class tree
		multipleClassDef parseStart = completeParse;
		//begin parse
		String token = LexAnalyzer.getToken(br);
		String[] token_split = token.split(" ");
		while(true){
			if(token_split[1].equals("EOF")){
				break;
			}
			if(token_split[0].equals("class")){
				System.setOut(debug);
				System.out.println("class found");
				System.setOut(classOutput);
				//begin next step of parse
				token = LexAnalyzer.getToken(br);
				token_split = token.split(" ");
				if(token_split[1].equals("id")){
					System.setOut(debug);
					System.out.println("Class Name Found:"+token_split[0]);
					System.setOut(classOutput);
					parseStart.classdef = new classDef();
					parseStart.classdef.classname = new className();
					parseStart.classdef.classname.id = token_split[0];
					parseStart.classInfo.className = token_split[0];
					//parse if either superclass or if start of inside;
					token = LexAnalyzer.getToken(br);
					token_split = token.split(" ");
					//super class
					if(token_split[0].equals(":")){
						System.setOut(debug);
						System.out.println("Super Class Found");
						System.setOut(classOutput);
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
						if(token_split[1].equals("id")){
							System.setOut(debug);
							System.out.println("Super Class Name Found:"+token_split[0]);
							System.setOut(classOutput);
							//continue program
							//modification to original, going to add super class fields to the current
							parseStart.classdef.superClassName = new className();
							parseStart.classdef.superClassName.id = token_split[0];
							parseStart.classInfo.superClassName = token_split[0];
							multipleClassDef findSuper = completeParse;
							boolean failedToFind = true;
							while(findSuper.multiclassdef != null){
								if(findSuper.classInfo.className.equals(parseStart.classdef.superClassName.id)){
									failedToFind = false;
									parseStart.classInfo.fields = (LinkedList<String>)findSuper.classInfo.fields.clone();
									parseStart.classInfo.funMap = (HashMap<String, LinkedList<String>>)findSuper.classInfo.funMap.clone();
									parseStart.classInfo.funBodyMap = (HashMap<String, String>)findSuper.classInfo.funBodyMap.clone();
									break;
								}
								findSuper = findSuper.multiclassdef;
							}
							if(failedToFind){
								System.setOut(classOutput);
								System.out.println(token_split[0] + " Syntax Error, Super Class never initalized");
								return null;
							}
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
						}
						else{
							System.setOut(classOutput);
							System.out.println(token_split[0] + " Syntax Error, class identifier name expected");
							return null;
						}
					}
					//open bracket
					if(token_split[0].equals("{")){
						System.setOut(debug);
						System.out.println("Open Bracket Found");
						System.setOut(classOutput);
						token = LexAnalyzer.getToken(br);
						token_split = token.split(" ");
						parseStart.classdef.classbody = new classBody();
						parseStart.classdef.classbody.multifieldvarlist = new multiFieldVarList();
						multiFieldVarList fields = parseStart.classdef.classbody.multifieldvarlist;
						
						for(int i=0; i<parseStart.classInfo.fields.size();i++){
							fields.fieldvar = new fieldVar();
							fields.fieldvar.id = parseStart.classInfo.fields.get(i);
							fields.multifieldvarlist = new multiFieldVarList();
							fields = fields.multifieldvarlist;
						}
						
						while(token_split[1].equals("id")){
							//System.out.println("Field Variable Found");
							if(parseStart.classInfo.fields.contains(token_split[0])){
								continue;
							}
							else{
								parseStart.classInfo.fields.add(token_split[0]);
							}
							//add token to external structure and parse tree
							fields.fieldvar = new fieldVar();
							fields.fieldvar.id = token_split[0];
							//begin recursive call
							fields.multifieldvarlist = new multiFieldVarList();
							fields = fields.multifieldvarlist;
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
						}
						//end of multi recursive, make null
						fields = null;
						
						//temp to print
						System.setOut(debug);
						multiFieldVarList printFields = parseStart.classdef.classbody.multifieldvarlist;
						while(printFields.multifieldvarlist != null){
							System.out.println(printFields.fieldvar.id);
							printFields = printFields.multifieldvarlist;
						}
						System.setOut(classOutput);
						//begin function definitions
						parseStart.classdef.classbody.multifundeflist = new multiFunDefList();
						multiFunDefList funcdef = parseStart.classdef.classbody.multifundeflist;
						while(token_split[0].equals("(")){
							System.setOut(debug);
							System.out.println("Open Paren. Found");
							System.setOut(classOutput);
							//individual function parser
							if(token_split[0].equals("(")){
								int parenCount = 1;
								token = LexAnalyzer.getToken(br);
								token_split = token.split(" ");
								//header open paren
								if(token_split[0].equals("(")){
									parenCount++;
									token = LexAnalyzer.getToken(br);
									token_split = token.split(" ");
									//fun name gotten
									if(token_split[1].equals("id")){
										System.setOut(debug);
										System.out.println("Function Name Found " + token);
										System.setOut(classOutput);
										//LL for params
										LinkedList<String> params = new LinkedList<String>();
										funcdef.fundef = new funDef();
										funcdef.fundef.head = new header();
										funcdef.fundef.head.funname = new funName();
										funcdef.fundef.head.funname.id = token_split[0];
										//get params
										token = LexAnalyzer.getToken(br);
										token_split = token.split(" ");
										//set up recursive for param list
										funcdef.fundef.head.multiparamlist = new multiParameterList();
										multiParameterList paramsList = funcdef.fundef.head.multiparamlist;
										while(!token_split[0].equals(")")){
											if(token_split[1].equals("id")){
												System.setOut(debug);
												System.out.println("Function Parameter Found " + token);
												System.setOut(classOutput);
												//add param to tree and LL
												paramsList.param = new parameter();
												paramsList.param.id = token_split[0];
												params.add(token_split[0]);
												//recursive param list
												paramsList.multiparamlist = new multiParameterList();
												paramsList = paramsList.multiparamlist;
												//next token
												token = LexAnalyzer.getToken(br);
												token_split = token.split(" ");
											}
											else{
												//error on param name
												System.setOut(classOutput);
												System.out.println(token_split[0] + " Syntax Error, expected parameter");
												return null;
											}
										}
										token = LexAnalyzer.getToken(br);
										token_split = token.split(" ");
										//subtrack param
										parenCount--;
										//terminate params
										paramsList = null;
										//add func def to hashmap
										if(parseStart.classInfo.funMap.containsKey(funcdef.fundef.head.funname.id)){
											parseStart.classInfo.funMap.remove(funcdef.fundef.head.funname.id);
										}
										parseStart.classInfo.funMap.put(funcdef.fundef.head.funname.id, params);
										//parse expressions
										//set up expression holder
										funcdef.fundef.expression = new exp();
										exp expressions = funcdef.fundef.expression;
										String functionBody = "";
										//recursive function call
										functionBody = parseExpression(token, token_split, expressions, br, params, parseStart.classInfo.fields,functionBody,classOutput,debug);
										//check next character
										token = LexAnalyzer.getToken(br);
										token_split = token.split(" ");
										if(token_split[0].equals(")")){
											System.setOut(debug);
											System.out.println("Function Completed");
											System.setOut(classOutput);
											//check if overriding function
											if(parseStart.classInfo.funBodyMap.containsKey(funcdef.fundef.head.funname.id)){
												parseStart.classInfo.funBodyMap.remove(funcdef.fundef.head.funname.id);
											}
											parseStart.classInfo.funBodyMap.put(funcdef.fundef.head.funname.id, functionBody);
											//function is complete check if another function is present
											token = LexAnalyzer.getToken(br);
											token_split = token.split(" ");
											if(token_split[0].equals("(")){
												continue;
											}
											//end of class
											else if(token_split[0].equals("}")){
												funcdef = funcdef.multifundeflist;
												break;
											}
											else{
												//error expected end
												System.setOut(classOutput);
												System.out.println(token_split[0] + " Syntax Error, expected another function / end of class");
												return null;
											}
										}
									}
									else{
										//error on func name
										System.setOut(classOutput);
										System.out.println(token_split[0] + " Syntax Error, expected function name");
										return null;
									}
								}
								else{
									//error expected header start
									System.setOut(classOutput);
									System.out.println(token_split[0] + " Syntax Error, expected function header");
									return null;
								}
							}
							else{
								//error expected function start
								System.setOut(classOutput);
								System.out.println(token_split[0] + " Syntax Error, expected function start");
								return null;
							}
							funcdef = funcdef.multifundeflist;
						}
						funcdef = null;
						//if no functions or functions ended
						if(token_split[0].equals("}")){
							token = LexAnalyzer.getToken(br);
							token_split = token.split(" ");
						}
						//error something other than function start or end of class
						else{
							System.setOut(classOutput);
							System.out.println(token_split[0] + " Syntax Error, " + "}"  + " expected");
							return null;
						}
					}
					//error
					else{
						System.setOut(classOutput);
						System.out.println(token_split[0] + " Syntax Error, " + "{" + "expected");
						return null;
					}
				}
				else{
					//error if id not specified
					System.setOut(classOutput);
					System.out.println(token_split[0] + " Syntax Error, class identifier name expected");
					return null;
				}
			}
			else{
				//error
				System.setOut(classOutput);
				System.out.println(token_split[0] + " : Syntax Error, unexpected symbol where " + "class" + " expected");
				return null;
			}
			parseStart.multiclassdef = new multipleClassDef();
			parseStart = parseStart.multiclassdef;
			parseStart.classInfo = new ClassDefEntry();
		}
		parseStart = null;
		//finale
		System.setOut(classOutput);
		multipleClassDef printOut = completeParse;
		while(printOut.multiclassdef != null){
			System.out.println(printOut.classInfo);
			printOut = printOut.multiclassdef;
			System.out.println();
		}
		return completeParse;
	}
}

//class definitions
//information class
class ClassDefEntry extends multipleClassDef// symbol table entry for a single class def
{
	String className = "";
	String superClassName = ""; // value is "" if superclass is absent
	LinkedList<String> fields = new LinkedList<String>();
	HashMap<String, LinkedList<String>> funMap = new HashMap<String, LinkedList<String>>();
	HashMap<String, String> funBodyMap = new HashMap<String,String>();
		// function names mapped to their parameters

	public String toString()
	{
		return className + "=\nsuperclass=" + superClassName + "\nfields=" + fields.toString() + "\nfunctions=" + funMap.toString() + "\nfunction bodies=" + funBodyMap.toString();
	}
}
//parse classes
class multipleClassDef{
	classDef classdef;
	multipleClassDef multiclassdef;
	ClassDefEntry classInfo; 
}
class classDef extends multipleClassDef{
	className classname;
	className superClassName;
	classBody classbody;
}
class className extends classDef{
	String id;
}
class classBody extends classDef{
	multiFieldVarList multifieldvarlist;
	multiFunDefList multifundeflist;
}

//field var list
class multiFieldVarList extends classBody{
	fieldVar fieldvar;
	multiFieldVarList multifieldvarlist;
}
class fieldVar extends multiFieldVarList{
	String id;
}

//function definition
class multiFunDefList extends classBody{
	funDef fundef;
	multiFunDefList multifundeflist;
}
class funDef extends multiFunDefList{
	header head;
	exp expression;
}
class header extends funDef{
	funName funname;
	multiParameterList multiparamlist;
}
class funName extends header{
	String id;
}

//parameter
class multiParameterList extends header{
	parameter param;
	multiParameterList multiparamlist;
}
class parameter extends multiParameterList{
	String id;
}
class exp extends funDef{
	String id;
	String integer;
	String decimal;
	String keyword;
	funExp funexp;
}
class funExp extends exp{
	funCall funcall;
	binaryExp binexp;
	cond condition;
	not notType;
}
class funCall extends funExp{
	funName funname;
	multiExpList multiexplist;
}

//expresion 
class multiExpList extends funCall{
	exp expression;
	multiExpList multiexplist;
}
class binaryExp extends funExp{
	arithExp arithexp;
	boolExp boolexp;
	compExp compexp;
	dotExp dotexp;
}
class arithExp extends binaryExp{
	arithOp arithop;
	exp exp1;
	exp exp2;
}
class boolExp extends binaryExp{
	boolOp boolop;
	exp exp1;
	exp exp2;
}
class compExp extends binaryExp{
	compOp compop;
	exp exp1;
	exp exp2;
}
class dotExp extends binaryExp{
	exp exp1;
	exp exp2;
}
class cond extends funExp{
	exp exp1;
	exp exp2;
	exp exp3;
}
class not extends funExp{
	exp exp1;
}
class arithOp extends arithExp{
	String operator;
}
class boolOp extends boolExp{
	String operator;
}
class compOp extends compExp{
	String operator;
}