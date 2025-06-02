package com.lambda.parser;

import com.lambda.parser.ast.*;

public interface Visitor<T> {
    T visitLambda(LambdaExpr expr);
    T visitVar(VarExpr expr);
    T visitApp(AppExpr expr);
}