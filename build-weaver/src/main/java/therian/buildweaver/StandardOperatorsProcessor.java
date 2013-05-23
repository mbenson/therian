/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian.buildweaver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.kohsuke.MetaInfServices;

/**
 * Creates package-access class therian.StandardOperators with all elements annotated by {@link StandardOperator}.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("therian.buildweaver.StandardOperator")
public class StandardOperatorsProcessor extends AbstractProcessor {
    public static final String TARGET_CLASSNAME = "therian.StandardOperators";
    public static final String TEMPLATE_RESOURCE = "/therian/StandardOperators";

    private final Set<Element> originatingElements = new LinkedHashSet<Element>();
    private final Set<String> operators = new LinkedHashSet<String>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            ClassUtils.getClass(TARGET_CLASSNAME);
            processingEnv.getMessager().printMessage(Kind.ERROR, String.format("%s already exists", TARGET_CLASSNAME));
            return false;
        } catch (ClassNotFoundException e) {
        }

        if (roundEnv.processingOver()) {
            write();
            return true;
        }
        for (TypeElement ann : annotations) {
            final Set<? extends Element> standardOperatorElements = roundEnv.getElementsAnnotatedWith(ann);
            originatingElements.addAll(standardOperatorElements);

            for (Element element : standardOperatorElements) {
                if (!isValidStandardOperator(element)) {
                    throw new IllegalStateException(String.format("%s is not a valid @StandardOperator",
                        appendTo(new StringBuilder(), element).toString()));
                }
                if (element.getKind() == ElementKind.CLASS) {
                    operators.add(appendTo(new StringBuilder("new "), element).append("()").toString());
                }
                if (element.getKind() == ElementKind.METHOD) {
                    operators.add(appendTo(new StringBuilder(), element).append("()").toString());
                }
                if (element.getKind() == ElementKind.FIELD) {
                    operators.add(appendTo(new StringBuilder(), element).toString());
                }
            }
        }
        return true;
    }

    private void write() {
        InputStream templateStream = null;
        Writer targetWriter = null;
        try {
            templateStream = getClass().getResourceAsStream(TEMPLATE_RESOURCE);
            final String template = IOUtils.toString(templateStream, CharEncoding.UTF_8);
            final String output =
                StrSubstitutor.replace(template,
                    Collections.singletonMap("operators", StringUtils.join(operators, ",\n")));
            final JavaFileObject target =
                processingEnv.getFiler().createSourceFile(TARGET_CLASSNAME,
                    originatingElements.toArray(new Element[originatingElements.size()]));
            targetWriter = target.openWriter();
            IOUtils.write(output, targetWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(templateStream);
            IOUtils.closeQuietly(targetWriter);
        }

    }

    private static ExecutableElement findDefaultConstructor(TypeElement t) {
        for (Element element : t.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement cs = (ExecutableElement) element;
                if (cs.getParameters().size() == 0 && cs.getModifiers().contains(Modifier.PUBLIC)) {
                    return cs;
                }
            }
        }
        return null;
    }

    /**
     * Must be a public static concrete class with a default constructor, public static zero-arg method, or public
     * static final field.
     *
     * @param e
     * @return boolean
     */
    private static boolean isValidStandardOperator(final Element e) {
        if (e.getKind() == ElementKind.FIELD) {
            return e.getModifiers().containsAll(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL));
        }
        if (e.getKind() == ElementKind.METHOD) {
            return e.getModifiers().containsAll(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                && ((ExecutableElement) e).getParameters().isEmpty();
        }
        if (e.getKind() == ElementKind.CLASS) {
            if (e.getModifiers().contains(Modifier.ABSTRACT) || findDefaultConstructor((TypeElement) e) == null) {
                return false;
            }
            Element current = e;
            while (current.getKind() == ElementKind.CLASS) {
                final TypeElement t = (TypeElement) current;
                if (t.getNestingKind() == NestingKind.TOP_LEVEL) {
                    return true;
                }
                if (t.getNestingKind() == NestingKind.MEMBER && t.getModifiers().contains(Modifier.STATIC)) {
                    current = t.getEnclosingElement();
                    continue;
                }
                break;
            }
        }
        return false;
    }

    private static StringBuilder appendTo(StringBuilder buf, Element e) {
        final Element parent = e.getEnclosingElement();
        if (parent != null) {
            appendTo(buf, parent).append('.');
        }
        return buf.append(e.getKind() == ElementKind.PACKAGE ? ((PackageElement) e).getQualifiedName() : e
            .getSimpleName());
    }
}
