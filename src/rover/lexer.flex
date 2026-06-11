package rover;

import java_cup.runtime.Symbol;

%%

%class Lexer
%public
%unicode
%cup
%line
%column

%{
  /* Métodos auxiliares o variables de estado si fueran necesarios */
%}

/* Expresiones regulares */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Numero         = [0-9]+

%%

<YYINITIAL> {
  /* Palabras clave del Rover */
  "MOVE"      { return new Symbol(sym.MOVE, yyline + 1, yycolumn + 1); }
  "FORWARD"   { return new Symbol(sym.FORWARD, yyline + 1, yycolumn + 1); }
  "BACKWARD"  { return new Symbol(sym.BACKWARD, yyline + 1, yycolumn + 1); }
  "LEFT"      { return new Symbol(sym.LEFT, yyline + 1, yycolumn + 1); }
  "RIGHT"     { return new Symbol(sym.RIGHT, yyline + 1, yycolumn + 1); }
  "TURN"      { return new Symbol(sym.TURN, yyline + 1, yycolumn + 1); }
  "TAKE"      { return new Symbol(sym.TAKE, yyline + 1, yycolumn + 1); }
  "SAMPLE"    { return new Symbol(sym.SAMPLE, yyline + 1, yycolumn + 1); }
  "FROM"      { return new Symbol(sym.FROM, yyline + 1, yycolumn + 1); }
  "SOIL"      { return new Symbol(sym.SOIL, yyline + 1, yycolumn + 1); }
  "ROCK"      { return new Symbol(sym.ROCK, yyline + 1, yycolumn + 1); }
  "METERS"    { return new Symbol(sym.METERS, yyline + 1, yycolumn + 1); }
  "DEGREES"   { return new Symbol(sym.DEGREES, yyline + 1, yycolumn + 1); }
  
  /* Delimitador */
  ";"         { return new Symbol(sym.PUNTO_COMA, yyline + 1, yycolumn + 1); }
  
  /* Macro para números enteros */
  {Numero}    { return new Symbol(sym.NUMERO, yyline + 1, yycolumn + 1, Integer.valueOf(yytext())); }
  
  /* Ignorar espacios en blanco y saltos de línea */
  {WhiteSpace} { /* No hacer nada */ }
  
  /* Manejo de errores léxicos para capturar caracteres desconocidos */
  [^]         { 
                throw new RuntimeException("Error léxico: Carácter ilegal '" + yytext() + "' en línea " + (yyline + 1) + ", columna " + (yycolumn + 1)); 
              }
}
