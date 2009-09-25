#ifdef PERL_CORE

/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton interface for Bison's Yacc-like parsers in C
   
      Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.
   
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.
   
   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */


/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     WORD = 258,
     METHOD = 259,
     FUNCMETH = 260,
     THING = 261,
     PMFUNC = 262,
     PRIVATEREF = 263,
     FUNC0SUB = 264,
     UNIOPSUB = 265,
     LSTOPSUB = 266,
     LABEL = 267,
     FORMAT = 268,
     SUB = 269,
     ANONSUB = 270,
     PACKAGE = 271,
     USE = 272,
     WHILE = 273,
     UNTIL = 274,
     IF = 275,
     UNLESS = 276,
     ELSE = 277,
     ELSIF = 278,
     CONTINUE = 279,
     FOR = 280,
     GIVEN = 281,
     WHEN = 282,
     DEFAULT = 283,
     LOOPEX = 284,
     DOTDOT = 285,
     YADAYADA = 286,
     FUNC0 = 287,
     FUNC1 = 288,
     FUNC = 289,
     UNIOP = 290,
     LSTOP = 291,
     RELOP = 292,
     EQOP = 293,
     MULOP = 294,
     ADDOP = 295,
     DOLSHARP = 296,
     DO = 297,
     HASHBRACK = 298,
     NOAMP = 299,
     LOCAL = 300,
     MY = 301,
     MYSUB = 302,
     REQUIRE = 303,
     COLONATTR = 304,
     PREC_LOW = 305,
     DOROP = 306,
     OROP = 307,
     ANDOP = 308,
     NOTOP = 309,
     ASSIGNOP = 310,
     DORDOR = 311,
     OROR = 312,
     ANDAND = 313,
     BITOROP = 314,
     BITANDOP = 315,
     SHIFTOP = 316,
     MATCHOP = 317,
     REFGEN = 318,
     UMINUS = 319,
     POWOP = 320,
     POSTDEC = 321,
     POSTINC = 322,
     PREDEC = 323,
     PREINC = 324,
     ARROW = 325,
     PEG = 326
   };
#endif

/* Tokens.  */
#define WORD 258
#define METHOD 259
#define FUNCMETH 260
#define THING 261
#define PMFUNC 262
#define PRIVATEREF 263
#define FUNC0SUB 264
#define UNIOPSUB 265
#define LSTOPSUB 266
#define LABEL 267
#define FORMAT 268
#define SUB 269
#define ANONSUB 270
#define PACKAGE 271
#define USE 272
#define WHILE 273
#define UNTIL 274
#define IF 275
#define UNLESS 276
#define ELSE 277
#define ELSIF 278
#define CONTINUE 279
#define FOR 280
#define GIVEN 281
#define WHEN 282
#define DEFAULT 283
#define LOOPEX 284
#define DOTDOT 285
#define YADAYADA 286
#define FUNC0 287
#define FUNC1 288
#define FUNC 289
#define UNIOP 290
#define LSTOP 291
#define RELOP 292
#define EQOP 293
#define MULOP 294
#define ADDOP 295
#define DOLSHARP 296
#define DO 297
#define HASHBRACK 298
#define NOAMP 299
#define LOCAL 300
#define MY 301
#define MYSUB 302
#define REQUIRE 303
#define COLONATTR 304
#define PREC_LOW 305
#define DOROP 306
#define OROP 307
#define ANDOP 308
#define NOTOP 309
#define ASSIGNOP 310
#define DORDOR 311
#define OROR 312
#define ANDAND 313
#define BITOROP 314
#define BITANDOP 315
#define SHIFTOP 316
#define MATCHOP 317
#define REFGEN 318
#define UMINUS 319
#define POWOP 320
#define POSTDEC 321
#define POSTINC 322
#define PREDEC 323
#define PREINC 324
#define ARROW 325
#define PEG 326



#endif /* PERL_CORE */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef union YYSTYPE
{

/* Line 1676 of yacc.c  */

    I32	ival; /* __DEFAULT__ (marker for regen_perly.pl;
				must always be 1st union member) */
    char *pval;
    OP *opval;
    GV *gvval;
#ifdef PERL_IN_MADLY_C
    TOKEN* p_tkval;
    TOKEN* i_tkval;
#else
    char *p_tkval;
    I32	i_tkval;
#endif
#ifdef PERL_MAD
    TOKEN* tkval;
#endif



/* Line 1676 of yacc.c  */
} YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
#endif




