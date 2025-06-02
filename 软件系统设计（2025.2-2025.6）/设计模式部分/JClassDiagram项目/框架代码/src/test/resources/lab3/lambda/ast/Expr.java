package com.lambda.parser.ast;

public interface Expr {
    <T> T accept(Visitor<T> visitor);
}