package com.lambda.parser;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos;
    
    public Lexer(String input) {
        this.input = input.replaceAll("\\s+", "");
        this.pos = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            switch (c) {
                case '\\':
                    tokens.add(new Token(TokenType.LAMBDA));
                    pos++;
                    break;
                case '.':
                    tokens.add(new Token(TokenType.DOT));
                    pos++;
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN));
                    pos++;
                    break;
                default:
                    if (Character.isLetter(c)) {
                        tokens.add(parseIdentifier());
                    } else {
                        throw new RuntimeException("Unexpected character: " + c);
                    }
            }
        }
        return tokens;
    }

    private Token parseIdentifier() {
        int start = pos;
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            pos++;
        }
        return new Token(TokenType.IDENTIFIER, input.substring(start, pos));
    }
}

enum TokenType { LAMBDA, DOT, LPAREN, RPAREN, IDENTIFIER }

class Token {
    public final TokenType type;
    public final String value;

    Token(TokenType type) {
        this(type, "");
    }

    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}