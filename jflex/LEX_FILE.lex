/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/

import java_cup.runtime.*;

/******************************/
/* DOLLAR DOLLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/*******************************************************************************/
/* Note that this has to be the EXACT same name of the class the CUP generates */
/*******************************************************************************/
%cupsym TokenNames

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine() { return yyline + 1; } 

	/**********************************************/
	/* Enable token position extraction from main */
	/**********************************************/
	public int getTokenStartPosition() { return yycolumn + 1; }

	public int checkAndReturnInt() {
		try {
			int val = Integer.valueOf(yytext());
		
			if (val >= 32768 || val < 0) {
				throw new Error("Invalid integer, size not in range 0 <= x <= 2^15-1");
			}
			return val;
		} catch (NumberFormatException e) {
			throw new Error("Invalid integer");
		}
	}
%}

%x COMMENT

/***********************/
/* MACRO DECLARATIONS */
/***********************/
LineTerminator	= \r|\n|\r\n
WhiteSpace		= {LineTerminator} | [ \t\f]
INTEGER			= 0 | [1-9][0-9]*
INVALID_INTEGER = 0[0-9]+
LETTERS			= [a-zA-Z]
ID				= {LETTERS}+[a-zA-Z0-9]*
T1_COMMENT_CONTENT = [ \t\fa-zA-Z0-9\(\)\[\]\{\}\?\!\+\-\*/\.\;]
T2_COMMENT_CONTENT = [ \r\n\t\fa-zA-Z0-9\(\)\[\]\{\}\?\!\+\-/\.\;]
T1_COMMENT  	= "//"{T1_COMMENT_CONTENT}*{LineTerminator}
ILLEGAL_T1_COMMENT = "//"[^(\r|\n|\r\n)]*{LineTerminator}



/******************************/
/* DOLLAR DOLLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/************************/
/* Handling T2 comments */
/************************/

"/*"							{ yybegin(COMMENT); }
<COMMENT>"*/"					{ yybegin(YYINITIAL); }
<COMMENT>{T2_COMMENT_CONTENT}+ 	{ }
<COMMENT>"*"					{ }
<COMMENT><<EOF>>       			{ throw new Error("Unclosed t2 comment"); }

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {
"("					{ return symbol(TokenNames.LPAREN);}
")"					{ return symbol(TokenNames.RPAREN);}
"["					{ return symbol(TokenNames.LBRACK);}
"]"					{ return symbol(TokenNames.RBRACK);}
"{"					{ return symbol(TokenNames.LBRACE);}
"}"					{ return symbol(TokenNames.RBRACE);}
"+"					{ return symbol(TokenNames.PLUS);}
"-"					{ return symbol(TokenNames.MINUS);}
"*"					{ return symbol(TokenNames.TIMES);}
"/"					{ return symbol(TokenNames.DIVIDE);}
","					{ return symbol(TokenNames.COMMA);}
"."					{ return symbol(TokenNames.DOT);}
";"					{ return symbol(TokenNames.SEMICOLON);}
int					{ return symbol(TokenNames.TYPE_INT);}
string				{ return symbol(TokenNames.TYPE_STRING);}
void				{ return symbol(TokenNames.TYPE_VOID);}
":="				{ return symbol(TokenNames.ASSIGN);}
"="					{ return symbol(TokenNames.EQ);}
"<"					{ return symbol(TokenNames.LT);}
">"					{ return symbol(TokenNames.GT);}
array				{ return symbol(TokenNames.ARRAY);}
class				{ return symbol(TokenNames.CLASS);}
return				{ return symbol(TokenNames.RETURN);}
while				{ return symbol(TokenNames.WHILE);}
if					{ return symbol(TokenNames.IF);}
else				{ return symbol(TokenNames.ELSE);}
new					{ return symbol(TokenNames.NEW);}
extends				{ return symbol(TokenNames.EXTENDS);}
nil					{ return symbol(TokenNames.NIL);}
{INTEGER}			{ return symbol(TokenNames.INT, checkAndReturnInt());}
{INVALID_INTEGER}	{ throw new Error("Illegal integer"); }
\"{LETTERS}*\"		{ return symbol(TokenNames.STRING, yytext());}
{ID}				{ return symbol(TokenNames.ID,     yytext());}
{T1_COMMENT} 		{ /* skip as this is a comment */}
{ILLEGAL_T1_COMMENT} { throw new Error("Illegal character in comment"); }
{WhiteSpace}		{ /* just skip what was found, do nothing */ }
<<EOF>>				{ return symbol(TokenNames.EOF);}
[^]					{ throw new Error("Illegal character <"+yytext()+">"); }
}
