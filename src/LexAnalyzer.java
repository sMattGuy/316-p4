import java.io.*;

public class LexAnalyzer{
	public static void main(String[] args) throws FileNotFoundException, IOException{
		//error if arguments arent 1
		if(args.length != 1){
			System.out.println("Input must be 1 file\nUsage java LexAnalyzer input.txt");
			return;
		}
		//creation of file read
		File file = new File(args[0]);
		BufferedReader br = new BufferedReader(new FileReader(file));
		//creations of file output
		PrintStream o = new PrintStream(new File("output.txt"));
		PrintStream console = System.out;
		//token class creation
		token identity = new token();
		//setting output to output file
		System.setOut(o);
		//reading first character
		identity.characterNum = br.read();
		//reads the characters in until finished
		while(identity.characterNum != -1){
			identity.word = "";
			identity.iden = "";
			//simple token solver
			identity.identify();
			//starts parsing multi step tokens if not a simple token
			if(identity.iden.equals("advanced")){
				//start of complex tokens, each calls a token helper function
				//id
				if((identity.characterNum >= 65 && identity.characterNum <= 90)||(identity.characterNum >= 97 && identity.characterNum <= 122)){
					identity.id(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//int
				else if(identity.characterNum >= 48 && identity.characterNum <= 57){
					identity.integer(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//add
				else if(identity.characterNum == 43){
					identity.add(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//sub
				else if(identity.characterNum == 45){
					identity.sub(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//dot
				else if(identity.characterNum == 46){
					identity.dot(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//lt
				else if(identity.characterNum == 60){
					identity.lt(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//gt
				else if(identity.characterNum == 62){
					identity.gt(br);
					System.out.println(identity.word + " : " + identity.iden);
				}
				//control chars
				else if(identity.characterNum >= 0 && identity.characterNum <= 32){
					identity.characterNum = br.read();
				}
				//invalid token
				else{
					System.out.println((char)identity.characterNum + "\t:\tLexical Error, Invalid Token");
					identity.characterNum = br.read();
				}
			}
			//output if single character
			else{
				System.out.println((char)identity.characterNum + "\t:\t" + identity.iden);
				identity.characterNum = br.read();
			}
		}
		//closes file when done
		br.close();
	}
	
	static String getToken(BufferedReader br) throws IOException{
		//token class creation
		token identity = new token();
		//reading first character
		identity.characterNum = br.read();
		//end of file
		if(identity.characterNum == -1){
			return "-1 EOF";
		}
		//reads the characters in until finished
		identity.word = "";
		identity.iden = "";
		//simple token solver
		identity.identify();
		//starts parsing multi step tokens if not a simple token
		if(identity.iden.equals("advanced")){
			//start of complex tokens, each calls a token helper function
			//id
			if((identity.characterNum >= 65 && identity.characterNum <= 90)||(identity.characterNum >= 97 && identity.characterNum <= 122)){
				identity.id(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//int
			else if(identity.characterNum >= 48 && identity.characterNum <= 57){
				identity.integer(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//add
			else if(identity.characterNum == 43){
				identity.add(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//sub
			else if(identity.characterNum == 45){
				identity.sub(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//dot
			else if(identity.characterNum == 46){
				identity.dot(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//lt
			else if(identity.characterNum == 60){
				identity.lt(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//gt
			else if(identity.characterNum == 62){
				identity.gt(br);
				//System.out.println(identity.word + " : " + identity.iden);
				return identity.word + " " + identity.iden;
			}
			//control chars
			else if(identity.characterNum >= 0 && identity.characterNum <= 32){
				return getToken(br);
			}
			//invalid token
			else{
				System.out.println((char)identity.characterNum + "\t:\tLexical Error, Invalid Token");
				return Character.toString((char)identity.characterNum) + " ERROR";
			}
		}
		//output if single character
		else{
			//System.out.println((char)identity.characterNum + "\t:\t" + identity.iden);
			return Character.toString((char)identity.characterNum) + " " + identity.iden;
		}
	}
}
class token{
	//variables
	public String word;
	public int characterNum;
	public String iden;
	//constructor
	public token(){
		this.word = "";
		this.characterNum = 0;
		this.iden = "";
	}
	//methods
	//see if keyword, only called from id
	private boolean keywordCheck(){
		String[] keywords = {"class","if","null","this"};
		for(int i=0;i<keywords.length;i++){
			if(keywords[i].equals(this.word)){
				return true;
			}
		}
		return false;
	}
	//simple tokens, determins if multi char token is being parsed
	public void identify(){
		int input = this.characterNum;
		String type;
		switch(input){
			case 42: //*
			type = "mul";
			break;
			case 47: ///
			type = "div";
			break;
			case 40: //(
			type = "LParen";
			break;
			case 41: //)
			type = "RParen";
			break;
			case 123: //{
			type = "LBrace";
			break;
			case 125: //}
			type = "RBrace";
			break;
			case 61: //=
			type = "eq";
			break;
			case 124: //|
			type = "or";
			break;
			case 38: //&
			type = "and";
			break;
			case 33: //!
			type = "not";
			break;
			case 58: //:
			type = "colon";
			break;
			default:
			type = "advanced";
			break;
		}
		//sets identity of current token
		this.iden = type;
		return;
	}
	//advanced tokens
	//id
	public void id(BufferedReader br)throws IOException{
		//sets identity
		this.iden = "id";
		//while character is a digit or a letter
		while((this.characterNum >= 65 && this.characterNum <= 90)||(this.characterNum >= 97 && this.characterNum <= 122)||(this.characterNum >= 48 && this.characterNum <= 57)){
			//appends to full token name
			this.word = this.word + Character.toString((char)this.characterNum);
			//marks current position
			br.mark(16);
			//reads next character
			this.characterNum = br.read();
		}
		br.reset();
		//checks if current token is a keyword
		if(this.keywordCheck()){
			//if keyword, change identity
			this.iden = "keyword_"+this.word;
		}
		return;
	}
	//int
	public void integer(BufferedReader br)throws IOException{
		//change identity
		this.iden = "int";
		//while being parsed digit
		while(this.characterNum >= 48 && this.characterNum <= 57){
			//append to token name and advance character
			this.word = this.word + Character.toString((char)this.characterNum);
			br.mark(16);
			this.characterNum = br.read();
		}
		//if dot is next character
		if(this.characterNum == 46){
			//call float function
			this.floatType(br);
		}
		//if e is next character
		else if(this.characterNum == 69 || this.characterNum == 101){
			//call e function
			this.e(br);
		}
		br.reset();
		return;
	}
	//dot
	public void dot(BufferedReader br)throws IOException{
		//update identity
		this.iden = "dotOp";
		//append to current token
		this.word = this.word + Character.toString((char)this.characterNum);
		//advance character
		br.mark(16);
		this.characterNum = br.read();
		//if next char is a digit
		if(this.characterNum >= 48 && this.characterNum <= 57){
			//call float function
			this.floatType(br);
		}
		br.reset();
		return;
	}
	//add
	public void add(BufferedReader br)throws IOException{
		//change identity
		this.iden = "add";
		//append to current token
		this.word = this.word + Character.toString((char)this.characterNum);
		//advance character
		br.mark(16);
		this.characterNum = br.read();
		//if next char is a dot
		if(this.characterNum == 46){
			//cal decimal point function
			this.decimalPoint(br);
		}
		//if next char is a digit
		else if(this.characterNum >= 48 && this.characterNum <= 57){
			//call integer function
			this.integer(br);
		}
		br.reset();
		return;
	}
	//sub (exact same as add except for substitution 
	public void sub(BufferedReader br)throws IOException{
		//change identity
		this.iden = "sub";
		this.word = this.word + Character.toString((char)this.characterNum);
		br.mark(16);
		this.characterNum = br.read();
		//dot
		if(this.characterNum == 46){
			this.decimalPoint(br);
		}
		//digit
		else if(this.characterNum >= 48 && this.characterNum <= 57){
			this.integer(br);
		}
		br.reset();
		return;
	}
	//greater than
	public void gt(BufferedReader br)throws IOException{
		//update identity
		this.iden = "gt";
		//append to token
		this.word = this.word + Character.toString((char)this.characterNum);
		//read next char
		br.mark(16);
		this.characterNum = br.read();
		//if next char is equal sign
		if(characterNum == 61){
			//append equal to token
			this.word = this.word + Character.toString((char)this.characterNum);
			//update identity to greater than or equal
			this.iden = "ge";
			//read next char
			this.characterNum = br.read();
		}
		else{
			br.reset();
		}
		return;
	}
	//less than (exact same as greater then)
	public void lt(BufferedReader br)throws IOException{
		this.iden = "lt";
		this.word = this.word + Character.toString((char)this.characterNum);
		br.mark(16);
		this.characterNum = br.read();
		if(characterNum == 61){
			this.word = this.word + Character.toString((char)this.characterNum);
			this.iden = "le";
			this.characterNum = br.read();
		}
		else{
			br.reset();
		}
		return;
	}
	//float
	private void floatType(BufferedReader br)throws IOException{
		//update identity
		this.iden = "float";
		//if incoming dot
		if(this.characterNum == 46){
			this.word = this.word + Character.toString((char)this.characterNum);
			br.mark(16);
			this.characterNum = br.read();
		}
		//while character is a digit
		while(this.characterNum >= 48 && this.characterNum <= 57){
			this.word = this.word + Character.toString((char)this.characterNum);
			br.mark(16);
			this.characterNum = br.read();
		}
		//if character is an e or E
		if(this.characterNum == 101 || this.characterNum == 69){
			this.e(br);
		}
		br.reset();
		return;
	}
	//floate
	private void floatEType(BufferedReader br)throws IOException{
		//update identity
		this.iden = "floatE";
		//while character is a digit
		while(this.characterNum >= 48 && this.characterNum <= 57){
			this.word = this.word + Character.toString((char)this.characterNum);
			br.mark(16);
			this.characterNum = br.read();
		}
		br.reset();
		return;
	}
	//e and e + -
	private void e(BufferedReader br)throws IOException{
		//update token
		this.word = this.word + Character.toString((char)this.characterNum);
		br.mark(16);
		this.characterNum = br.read();
		//if character is + -
		if(this.characterNum == 43 || this.characterNum == 45){
			this.word = this.word + Character.toString((char)this.characterNum);
			br.mark(16);
			this.characterNum = br.read();
			//if character is a digit
			if(this.characterNum >= 48 && this.characterNum <= 57){
				this.floatEType(br);
			}
			//error throw if not a digit
			else{
				this.iden = "Lexical Error, Invalid Token";
				br.mark(16);
				this.characterNum = br.read();
			}
		}
		//if char is digit
		else if(this.characterNum >= 48 && this.characterNum <= 57){
			this.floatEType(br);
		}
		//error throw if not digit
		else{
			this.iden = "Lexical Error, Invalid Token";
			br.mark(16);
			this.characterNum = br.read();
		}
		br.reset();
		return;
	} 
	//decimal point
	private void decimalPoint(BufferedReader br)throws IOException{
		this.word = this.word + Character.toString((char)this.characterNum);
		br.mark(16);
		this.characterNum = br.read();
		//if character is a digit
		if(this.characterNum >= 48 && this.characterNum <= 57){
			this.floatType(br);
		}
		//error throw if not digit
		else{
			this.word = this.word + Character.toString((char)this.characterNum);
			this.iden = "Lexical Error, Invalid Token";
			br.mark(16);
			this.characterNum = br.read();
		}
		br.reset();
		return;
	}
}