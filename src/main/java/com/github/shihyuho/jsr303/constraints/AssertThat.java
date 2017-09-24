package com.github.shihyuho.jsr303.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;

import com.github.shihyuho.jsr303.AssertThatValidator;

/**
 * Assert the given expression true
 *
 * @author Matt S.Y. Ho
 */
@Documented
@Constraint(validatedBy = {AssertThatValidator.class})
@Target({TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
public @interface AssertThat {

  @interface Namespace {
    String prefix();

    Class<?> clazz();
  }

  String message() default "{com.github.shihyuho.jsr303.constraints.AssertThat.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** @return the expression to evaluate */
  String value();

  /**
   * bind more namespace to evaluate
   *
   * @return
   */
  Namespace[] namespaces() default {};

  /**
   * Indicate propertyNode for ConstraintViolation
   *
   * @return
   */
  String propertyNode() default "";

  Engine engine() default Engine.JEXL;

  enum Engine {
    JEXL,
    SpEL,
    ;
  }

  /** Defines several {@link AssertThat} annotations on the same element. */
  @Target({TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    AssertThat[] value();
  }
}
