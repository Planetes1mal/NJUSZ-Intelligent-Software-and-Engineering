package com.lambda.parser;

import com.lambda.parser.ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Expr parse() {
        return parseExpression();
    }

    private Expr parseExpression() {
        if (peek().type == TokenType.LAMBDA) {
            return parseLambda();
        }
        return parseApplication();
    }

    private LambdaExpr parseLambda() {
        consume(TokenType.LAMBDA);
        List<String> params = new ArrayList<>();
        params.add(consume(TokenType.IDENTIFIER).value);
        while (peek().type == TokenType.IDENTIFIER) {
            params.add(consume(TokenType.IDENTIFIER).value);
        }
        consume(TokenType.DOT);
        Expr body = parseExpression();
        return new LambdaExpr(params, body);
    }

    private Expr parseApplication() {
        Expr expr = parseAtom();
        while (peek().type == TokenType.LPAREN || 
               peek().type == TokenType.IDENTIFIER) {
            expr = new AppExpr(expr, parseAtom());
        }
        return expr;
    }

    private Expr parseAtom() {
        if (peek().type == TokenType.LPAREN) {
            consume(TokenType.LPAREN);
            Expr expr = parseExpression();
            consume(TokenType.RPAREN);
            return expr;
        }
        return new VarExpr(consume(TokenType.IDENTIFIER).value);
    }

    private Token consume(TokenType expected) {
        Token token = tokens.get(pos++);
        if (token.type != expected) {
            throw new RuntimeException("Expected " + expected + ", got " + token.type);
        }
        return token;
    }

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : new Token(TokenType.EOF);
    }
}