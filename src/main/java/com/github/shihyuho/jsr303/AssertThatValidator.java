package com.github.shihyuho.jsr303;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.shihyuho.jsr303.constraints.AssertThat;
import lombok.AllArgsConstructor;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

/**
 * implementation by using JEXL3 or SpEL
 *
 * @author Matt S.Y. Ho
 */
public class AssertThatValidator implements ConstraintValidator<AssertThat, Object> {

  private JexlEngine jexl;
  private String expression;
  private String message;
  private String propertyNode;
  private ExpressionParser expressionParser;
  private ParserContext parserContext;
  private Predicate<Object> validator;

  @Autowired(required = false)
  private BeanFactory beanFactory;

  @Override
  public void initialize(AssertThat constraintAnnotation) {
    expression = constraintAnnotation.value();
    message = constraintAnnotation.message();
    propertyNode = constraintAnnotation.propertyNode();

    switch (constraintAnnotation.engine()) {
      case JEXL:
        final Map<String, Object> namespaces = new HashMap<>();
        namespaces.put(null, Class.class);
        Stream.of(constraintAnnotation.namespaces())
            .forEach(ns -> namespaces.put(ns.prefix(), ns.clazz()));
        jexl =
            new JexlBuilder().cache(512).strict(true).silent(false).namespaces(namespaces).create();
        validator = this::validateByJexl;
        break;
      case SpEL:
        expressionParser = new SpelExpressionParser();
        parserContext = new TemplateParserContext();
        validator = this::validateBySpel;
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported EL engine: [%s]", constraintAnnotation.engine()));
    }
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    try {
      final boolean valid = validator.test(value);
      if (!valid && !StringUtils.isEmpty(propertyNode)) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(message)
            .addPropertyNode(propertyNode)
            .addConstraintViolation();
      }
      return valid;
    } catch (RuntimeException e) {
      throw new IllegalStateException(
          String.format("Failed to validate [%s] by expression '%s'", value, expression), e);
    }
  }

  boolean validateByJexl(Object value) {
    final JexlContext ctx = new MapContext();
    ctx.set("this", value);
    final Object evaluated = jexl.createExpression(expression).evaluate(ctx);
    if (!(evaluated instanceof Boolean)) {
      throw new IllegalArgumentException(
          String.format(
              "The expression [%s] should evaluate to boolean, but was '%s'",
              expression, evaluated.getClass().getName()));
    }
    final boolean valid = (boolean) evaluated;
    return valid;
  }

  boolean validateBySpel(Object value) {
    final StandardEvaluationContext evaluationContext =
        new StandardEvaluationContext(new ThisWrapper(value));
    evaluationContext.addPropertyAccessor(new MapAccessor());
    if (beanFactory != null) {
      evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }
    final boolean valid =
        expressionParser
            .parseExpression(expression, parserContext)
            .getValue(evaluationContext, boolean.class);
    return valid;
  }

  /** Wrapper class to expose an object to the SpEL expression as {@code this}. */
  @AllArgsConstructor
  static class ThisWrapper {
    private final Object object;

    public Object getThis() {
      return object;
    }
  }
}
