package com.suspend.annotation;

import com.suspend.mapping.FetchType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {
    String mappedBy();

    FetchType fetch() default FetchType.LAZY;
}
