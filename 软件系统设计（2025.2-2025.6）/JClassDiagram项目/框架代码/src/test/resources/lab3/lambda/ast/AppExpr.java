package com.lambda.parser.ast;

public class AppExpr implements Expr {
    public final Expr fun;
    public final Expr arg;

    public AppExpr(Expr fun, Expr arg) {
        this.fun = fun;
        this.arg = arg;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitApp(this);
    }
}