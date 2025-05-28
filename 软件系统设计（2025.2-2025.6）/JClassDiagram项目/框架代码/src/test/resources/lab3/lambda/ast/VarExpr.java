package com.lambda.parser.ast;

public class VarExpr implements Expr {
    public final String name;

    public VarExpr(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVar(this);
    }
}