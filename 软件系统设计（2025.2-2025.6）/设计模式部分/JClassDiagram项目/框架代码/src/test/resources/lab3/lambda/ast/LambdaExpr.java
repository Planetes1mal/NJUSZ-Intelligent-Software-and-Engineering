package com.lambda.parser.ast;

import java.util.List;

public class LambdaExpr implements Expr {
    public final List<String> params;
    public final Expr body;

    public LambdaExpr(List<String> params, Expr body) {
        this.params = params;
        this.body = body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitLambda(this);
    }
}