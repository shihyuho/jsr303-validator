package com.github.shihyuho.jsr303.constraints;

import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import javax.validation.*;
import java.lang.annotation.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** @author Matt S.Y. Ho */
public class AssertThatSpELTest {

  @AssertThat(
    engine = AssertThat.Engine.SpEL,
    value =
        "#{this.age > 0 && this.birthday != null "
            + "&& T(java.time.Period).between(this.birthday, T(java.time.LocalDate).now()).getYears() == this.age}"
  )
  @Getter
  public static class MyBean {
    int age;
    LocalDate birthday;
  }

  private Validator validator;

  @Before
  public void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  public void test() {
    MyBean object = new MyBean();
    object.age = 5;
    object.birthday = LocalDate.now().minusYears(object.age);
    Set<ConstraintViolation<MyBean>> results = validator.validate(object);
    Assert.assertTrue(results.isEmpty());
  }

  @AssertThat(
    engine = AssertThat.Engine.SpEL,
    value =
        "#{this.age >= 18 || (this.age < 18  && !T(org.springframework.util.StringUtils).isEmpty(this.parent))}",
    propertyNode = "age"
  )
  @Getter
  public class MyBean2 {
    int age;
    String parent;
  }

  @Test
  public void testPropertyNode() {
    MyBean2 object = new MyBean2();
    object.age = 19;
    Set<ConstraintViolation<MyBean2>> results = validator.validate(object);
    Assert.assertTrue(results.isEmpty());

    object.age = 15;
    results = validator.validate(object);
    Assert.assertFalse(results.isEmpty());
    Assert.assertEquals(1, results.size());
    System.out.println(results);
    results.forEach(v -> Assert.assertEquals("age", v.getPropertyPath().toString()));
  }

  @Documented
  @Constraint(validatedBy = {})
  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  @AssertThat(
    engine = AssertThat.Engine.SpEL,
    value =
        "#{this != null && "
            + "T(java.util.Arrays).asList("
            + "T(com.github.shihyuho.jsr303.constraints.Grade).A, "
            + "T(com.github.shihyuho.jsr303.constraints.Grade).B"
            + ").contains(this)}"
  )
  public @interface GradeAboveB {

    String message() default
        "{com.github.shihyuho.jsr303.constraints.AssertThatSpELTest.GradeAboveB.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
  }

  @Documented
  @Constraint(validatedBy = {})
  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  @AssertThat(
    engine = AssertThat.Engine.SpEL,
    value =
        "#{this == null || " + "this.contains(T(com.github.shihyuho.jsr303.constraints.Grade).A)}"
  )
  public @interface ContainsGradeA {

    String message() default
        "{com.github.shihyuho.jsr303.constraints.AssertThatSpELTest.ContainsGradeA.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
  }

  @Getter
  public static class MyBean3 {
    @GradeAboveB Grade myGrade;

    @ContainsGradeA List<Grade> otherGrades;
  }

  @Test
  public void testCustomAnnotation() {
    MyBean3 object = new MyBean3();
    object.myGrade = Grade.A;
    Set<ConstraintViolation<MyBean3>> results = validator.validate(object);
    Assert.assertTrue(results.isEmpty());

    object.otherGrades = Arrays.asList(Grade.C, Grade.D);
    results = validator.validate(object);
    Assert.assertFalse(results.isEmpty());
    Assert.assertEquals(1, results.size());
  }
}
