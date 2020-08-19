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
package therian.position.relative.propertymethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.weaver.model.ScanRequest;
import org.apache.commons.weaver.model.ScanResult;
import org.apache.commons.weaver.model.Scanner;
import org.apache.commons.weaver.model.WeavableClass;
import org.apache.commons.weaver.model.WeaveEnvironment;
import org.apache.commons.weaver.model.WeaveEnvironment.Resource;
import org.apache.commons.weaver.spi.Weaver;
import org.kohsuke.MetaInfServices;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import therian.position.relative.Property;

/**
 * {@link Weaver} to wrap method reference lambdas:
 * <ul>
 * <li>Java bean property accessors as {@link Function}: to {@link Property.Accessor}</li>
 * <li>Java bean property mutators as {@link BiConsumer}: to {@link Property.Mutator}</li>
 * </ul>
 */
@MetaInfServices(Weaver.class)
public class PropertyMethodWeaver implements Weaver {
    /**
     * Convenience structure to hold the environment passed into {@link Weaver#process(WeaveEnvironment, Scanner)}.
     */
    private static class Worker {

        private final class CustomClassWriter extends ClassWriter {
            CustomClassWriter(final ClassReader classReader, final int flags) {
                super(classReader, flags);
            }

            @Override
            protected String getCommonSuperClass(final String type1, final String type2) {
                // https://gitlab.ow2.org/asm/asm/merge_requests/166
                ClassLoader classLoader = env.classLoader;
                Class<?> class1;
                try {
                    class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
                } catch (Exception e) {
                    throw new TypeNotPresentException(type1, e);
                }
                Class<?> class2;
                try {
                    class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
                } catch (Exception e) {
                    throw new TypeNotPresentException(type2, e);
                }
                if (class1.isAssignableFrom(class2)) {
                    return type1;
                }
                if (class2.isAssignableFrom(class1)) {
                    return type2;
                }
                if (class1.isInterface() || class2.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        class1 = class1.getSuperclass();
                    } while (!class1.isAssignableFrom(class2));
                    return class1.getName().replace('.', '/');
                }
            }
        }

        /**
         * Convenient {@link ClassVisitor} layer to write classfiles into the {@link WeaveEnvironment}.
         */
        class WriteClass extends ClassVisitor {

            private String className;

            WriteClass(final ClassReader classReader, final int flags) {
                super(Opcodes.ASM6, new CustomClassWriter(classReader, flags));
            }

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                this.className = name;
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                final byte[] bytecode = ((ClassWriter) cv).toByteArray();

                final Resource classfile = env.getClassfile(className);
                env.debug("Writing class %s to resource %s", className, classfile.getName());
                try (OutputStream outputStream = classfile.getOutputStream()) {
                    outputStream.write(bytecode);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private final WeaveEnvironment env;

        Worker(WeaveEnvironment env) {
            super();
            this.env = env;
        }

        /**
         * Perform the weaving operations for the specified {@link Class}.
         * 
         * @param type
         * @return whether any modification was made
         */
        boolean weave(Class<?> type) {
            env.debug("Wrapping property accessor/mutator method references in %s", type);

            try (final InputStream bytecode = env.getClassfile(type).getInputStream()) {
                final ClassReader reader = new ClassReader(bytecode);

                final ClassNode cn = new ClassNode(Opcodes.ASM6);
                final PropertyMethodWrappingClassVisitor cv = new PropertyMethodWrappingClassVisitor(env, cn);

                reader.accept(cv, ClassReader.EXPAND_FRAMES);

                if (cv.isModified()) {
                    // add marker annotation to sign our work, as it were:
                    cn.visitAnnotation(MARKER_ANNOTATION.getDescriptor(), false).visitEnd();
                    // write new classfile:
                    cn.accept(new WriteClass(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS));
                    return true;
                }
                env.debug("%s was unmodified", type);
                return false;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * ASM {@link Type} representing our marker annotation.
     */
    static final Type MARKER_ANNOTATION = Type.getType(WovenPropertyMethods.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(WeaveEnvironment environment, Scanner scanner) {

        // interested in all classes:
        final ScanResult scanResult = scanner.scan(new ScanRequest());

        final Worker w = new Worker(environment);
        boolean result = false;

        for (WeavableClass<?> weavableClass : scanResult.getClasses()) {
            result = w.weave(weavableClass.getTarget()) || result;
        }
        return result;
    }
}
