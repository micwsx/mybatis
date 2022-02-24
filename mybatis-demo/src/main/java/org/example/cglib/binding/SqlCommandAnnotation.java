package org.example.cglib.binding;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlCommandAnnotation {

    String commandType();
    String sqlText();
}
