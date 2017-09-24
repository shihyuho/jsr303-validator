# JSR303 Validators

A collection of jsr-303 validators I used

## AssertThat

Integration with [Apache commons JEXL](http://commons.apache.org/proper/commons-jexl/)

```java
@AssertThat(
	value = "this.age >= 18 || (this.age < 18  && not empty(this.parent))",
    propertyNode = "age")
@Data
public class MyBean {
	private int age;
	private String parent;
	@AssertThat("this != null && this.isAfter(forName('java.time.LocalDate').now().plusDays(7))")
	private LocalDate expire;
	// ...
}
```


- `this` - Represent the Object where *@AssertThat* located.

```java
@AssertThat("this != null") // 'this' referring to expire field
private LocalDate expire;
```

```java
@AssertThat("this != null") // 'this' referring to SomeObject
public class SomeObject {
}
```

- `namespace` - Indicate the namespace referring to a class.

```java
@AssertThat(
	value = "math:abs(this) > 10",
	namespaces = {
		@Namespace(prefix = "math", clazz = Math.class)
	}
)
private int index;
```
 
- `propertyNode` - Indecate a property node the *ConstraintViolation* will be associated to.

```java
@AssertThat(
	value = "this.age >= 18 || (this.age < 18  && not empty(this.parent))",
	propertyNode = "age")
public class MyBean {
	private int age;
	private String parent;
}
```

```java
ConstraintViolationImpl {
	interpolatedMessage="com.github.shihyuho.jsr303.constraints.AssertThat.message", 
	propertyPath="age",
	rootBeanClass=...,
	messageTemplate='com.github.shihyuho.jsr303.constraints.AssertThat.message'
}
```

Supports [Spring Expression Language](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html) as well

```java
@AssertThat(
	engine = AssertThat.Engine.SpEL, // JEXL is the defualt EL engine
	value =
	    "#{this.age >= 18 || (this.age < 18  && !T(org.springframework.util.StringUtils).isEmpty(this.parent))}",
	propertyNode = "age"
)
public class MyBean {
    private int age;
    private String parent;
}
```

### Custom Annotation

```java
public enum Grade {
  A, B, C, D, E, F, ;
}
```

```java
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@AssertThat(
  "this != null && " +
  "forName('java.util.Arrays').asList(" +
    "forName('com.github.shihyuho.jsr303.constraints.Grade').valueOf('A'), " +
    "forName('com.github.shihyuho.jsr303.constraints.Grade').valueOf('B')" +
  ").contains(this)")
public @interface GradeAboveB {
	String message() default "{com.github.shihyuho.jsr303.constraints.GradeAboveB.message}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
```

```java
@Data
public class PassExams {
	private Student student;
	private Exam exam;
	@GradeAboveB private Grade grade;
}
```
