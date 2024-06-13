package com.suspend.annotation.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

// @Note: check if this is even possible.
@SupportedAnnotationTypes({"com.suspend.annotation.Column", "com.suspend.annotation.Id"})
public class EntityMappingProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = env.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (element.getKind().equals(ElementKind.FIELD)) {
                    Element enclosingElement = element.getEnclosingElement();
                    if (enclosingElement.getKind().equals(ElementKind.CLASS)) {
//                        TypeElement entityClass = (TypeElement) enclosingElement;
//
//                        TypeMirror fieldType = element.asType();

//                        generateMapping(entityClass, element);
                    }
                }
            }
        }
        return true;
    }

}
