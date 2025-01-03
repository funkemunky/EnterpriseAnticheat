package dev.brighten.ac.utils.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AutoLoad {
    boolean commands() default true;
    boolean listeners() default true;
}
