package de.adito.beans.persistence.annotationprocessor;

import com.squareup.javapoet.*;
import de.adito.beans.core.*;
import de.adito.beans.persistence.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Annotation processor for {@link Persist}.
 * It parses all annotations of the project and creates static access fields for persistent beans and containers.
 * There will be a class for beans and an extra class for containers.
 * The name of the static fields will be obtained from {@link Persist#containerId()}.
 *
 * @author Simon Danner, 26.02.2018
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("de.adito.beans.persistence.Persist")
public class PersistenceAnnotationProcessor extends AbstractProcessor
{
  private static final String BEAN_BASE_TYPE = Bean.class.getName();
  private static final String BASE_PATH = "de.adito.beans.persistence";
  private static final Class RETRIEVER_CLASS = OJPersistence.class;
  private static final String RETRIEVER_METHOD = "dataStore()";
  private static final String BEAN_CLASS_NAME = "OJBeans";
  private static final String CONTAINER_CLASS_NAME = "OJContainers";
  private static final Function<Boolean, String> DATA_STORE_METHOD = pIsContainer -> pIsContainer ? "getContainerByPersistenceId" :
      "getBeanByPersistenceId";
  private static final Class ARRAYS_CLASS = Arrays.class;
  private static final String AS_LIST_METHOD = "asList";
  private static final Function<Boolean, String> OBSOLETE_REMOVER = pIsContainer -> pIsContainer ? "removeObsoleteBeanContainers" :
      "removeObsoleteSingleBeans";

  @Override
  public boolean process(Set<? extends TypeElement> pAnnotations, RoundEnvironment pRoundEnvironment)
  {
    final Map<String, Element> beans = new TreeMap<>();
    final Map<String, Element> containers = new TreeMap<>();
    final Types types = processingEnv.getTypeUtils();
    final TypeMirror beanSuperType = types.erasure(processingEnv.getElementUtils().getTypeElement(BEAN_BASE_TYPE).asType());

    pRoundEnvironment.getElementsAnnotatedWith(Persist.class).forEach(pElement -> {
      Persist annotation = pElement.getAnnotation(Persist.class);

      if (pElement.getKind() != ElementKind.CLASS || !types.isAssignable(pElement.asType(), beanSuperType))
        throw new RuntimeException("A persistence annotation can only be used with bean classes!");

      //Single bean or container?
      Map<String, Element> mapping = annotation.mode() == EPersistenceMode.CONTAINER ? containers : beans;

      final String containerId = annotation.containerId();
      if (mapping.containsKey(containerId))
        throw new RuntimeException("Persistent annotations cannot use the same persistence ID twice within the project! id: " + containerId);

      mapping.put(containerId, pElement);
    });

    if (!beans.isEmpty())
      _createClass(BEAN_CLASS_NAME, beans, false);
    if (!containers.isEmpty())
      _createClass(CONTAINER_CLASS_NAME, containers, true);
    return true;
  }

  /**
   * Creates a static access class for persistent bean elements.
   * The class will be able to provide static fields for persistent beans or containers marked by {@link Persist}.
   *
   * @param pClassName   the name of the class
   * @param pMapping     a mapping of all fields that should be created (containerId -> the annotated class)
   * @param pIsContainer <tt>true</tt>, if the fields should provide bean containers (not single beans)
   */
  private void _createClass(String pClassName, Map<String, Element> pMapping, boolean pIsContainer)
  {
    //Create static fields
    Set<FieldSpec> fieldSpecs = pMapping.entrySet().stream()
        .map(pEntry -> FieldSpec.builder(_getFieldType(pEntry.getValue(), pIsContainer), pEntry.getKey())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.$N.$N($S, $T.class)", RETRIEVER_CLASS, RETRIEVER_METHOD, DATA_STORE_METHOD.apply(pIsContainer),
                         pEntry.getKey(), ClassName.get(pEntry.getValue().asType()))
            .build())
        .collect(Collectors.toSet());

    final String allFields = String.join(", ", pMapping.keySet());
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(pClassName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addFields(fieldSpecs)
        .addStaticBlock(CodeBlock.builder()
                            .addStatement("$T.$N($T.$N(" + allFields + "))", RETRIEVER_CLASS, OBSOLETE_REMOVER.apply(pIsContainer),
                                          ARRAYS_CLASS, AS_LIST_METHOD)
                            .build());

    try
    {
      JavaFile javaFile = JavaFile.builder(BASE_PATH, classBuilder.build()).build();
      javaFile.writeTo(processingEnv.getFiler());
    }
    catch (IOException pE)
    {
      throw new RuntimeException("Unable to generate persistence classes!", pE);
    }
  }

  /**
   * The type name of a field based on the annotated bean type.
   * It's either the type itself (single bean) or a bean container, that contains beans of the annotated class type.
   *
   * @param pElement     the annotated class
   * @param pIsContainer <tt>true</tt>, if the field should provide a bean or a container (no single bean)
   * @return the type name
   */
  private TypeName _getFieldType(Element pElement, boolean pIsContainer)
  {
    TypeName beanType = TypeName.get(pElement.asType());
    return pIsContainer ? ParameterizedTypeName.get(ClassName.get(IBeanContainer.class), beanType) : beanType;
  }
}
