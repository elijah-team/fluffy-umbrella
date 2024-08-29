package com.baeldung.annotation.processor;

import com.google.auto.service.AutoService;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.baeldung.annotation.processor.BuilderProperty")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            Map<Boolean, List<Element>> annotatedMethods = annotatedElements.stream().collect(Collectors.partitioningBy(element -> ((ExecutableType) element.asType()).getParameterTypes().size() == 1 && element.getSimpleName().toString().startsWith("set")));

            List<Element> setters = annotatedMethods.get(true);
            List<Element> otherMethods = annotatedMethods.get(false);

            otherMethods.forEach(element -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@BuilderProperty must be applied to a setXxx method with a single argument", element));

            if (setters.isEmpty()) {
                continue;
            }

            String className = ((TypeElement) setters.get(0).getEnclosingElement()).getQualifiedName().toString();

            Map<String, String> setterMap = setters.stream().collect(Collectors.toMap(setter -> setter.getSimpleName().toString(), setter -> ((ExecutableType) setter.asType()).getParameterTypes().get(0).toString()));

            try {

                String packageName = null;
                int lastDot = className.lastIndexOf('.');
                if (lastDot > 0) {
                    packageName = className.substring(0, lastDot);
                }

                String simpleClassName = className.substring(lastDot + 1);
                String builderClassName = className + "Builder";
                String builderSimpleClassName = builderClassName.substring(lastDot + 1);

                final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
                javaClass
                        .setName(builderSimpleClassName)
                        .setPackage(packageName)
                        .setPublic();

                javaClass.addField()
                        .setName("object")
                        .setType(simpleClassName)
                        .setPrivate()
                        .setFinal(true)
                        .setLiteralInitializer("new " + simpleClassName + "();");

                javaClass.addMethod()
                        .setName("build")
                        .setReturnType(simpleClassName)
                        .setPublic()
                        .setBody("        return object;");

                setterMap.entrySet().forEach(setter -> {
                    String methodName = setter.getKey();
                    String argumentType = setter.getValue();

                    MethodSource<JavaClassSource> m = javaClass.addMethod()
                            .setName(methodName)
                            .setPublic()
                            .setReturnType(builderSimpleClassName);
                    m.addParameter(argumentType, "value");
                    m.setBody("this.object." + methodName + " (value);" + "return this;");
                });


                JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
                try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                    out.print(javaClass.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
