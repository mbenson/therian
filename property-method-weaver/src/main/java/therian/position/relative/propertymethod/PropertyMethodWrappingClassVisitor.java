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

import java.beans.Introspector;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.weaver.model.WeaveEnvironment;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import therian.position.relative.Property;

/**
 * ASM {@link ClassVisitor} to handle the bulk of the needed work.
 */
class PropertyMethodWrappingClassVisitor extends ClassVisitor {

    /**
     * Property method enumeration.
     */
    //@formatter:off
    enum PropertyMethod {
        /**
         * Represents accessors.
         */
        ACCESSOR(Function.class, Property.Accessor.class) {

            /**
             * {@inheritDoc}
             */
            @Override
            String prefixFrom(String methodName, Type methodType) {
                if (methodType.getArgumentTypes().length > 0 || methodType.getReturnType() == Type.VOID_TYPE) {
                    return null;
                } 
                return methodName.startsWith("is") && methodType.getReturnType() == Type.BOOLEAN_TYPE ? "is" : "get";
            }
        },
        /**
         * Represents mutators.
         */
        MUTATOR(BiConsumer.class, Property.Mutator.class) {

            /**
             * {@inheritDoc}
             */
            @Override
            String prefixFrom(String name, Type methodType) {
                return methodType.getArgumentTypes().length == 1 && methodType.getReturnType() == Type.VOID_TYPE
                        ? "set" : null;
            }
        };
        //@formatter:on

        final Method method;
        final Constructor<?> ctor;

        private PropertyMethod(Class<?> functionalInterface, Class<?> wrapper) {
            Validate.notNull(functionalInterface, "functionalInterface was null");
            Validate.isTrue(functionalInterface.isInterface(), "%s is not an interface", functionalInterface);

            final Set<Method> abstractMethods = Stream.of(functionalInterface.getMethods())
                .filter(m -> Modifier.isAbstract(m.getModifiers())).collect(Collectors.toSet());
            Validate.isTrue(abstractMethods.size() == 1, "%s has %d abstract methods; invalid functional interface",
                functionalInterface, abstractMethods.size());
            this.method = abstractMethods.iterator().next();
            Validate.notNull(wrapper, "wrapper class was null");
            Validate.isTrue(!Modifier.isAbstract(wrapper.getModifiers()), "%s is abstract", wrapper);
            Validate.isTrue(Modifier.isPublic(wrapper.getModifiers()), "%s is not public", wrapper);

            final Constructor<?>[] ctors = wrapper.getDeclaredConstructors();
            Validate.isTrue(ctors.length == 1, "Expected a single %s constructor, found %d", wrapper, ctors.length);
            this.ctor = ctors[0];
            final Class<?>[] parameterTypes = new Class[] { String.class, functionalInterface };
            Validate.isTrue(ClassUtils.isAssignable(parameterTypes, ctor.getParameterTypes()),
                "Expected %s to have parameters assignable from %s", ctor, Arrays.toString(parameterTypes));
        }

        /**
         * Learn whether this {@link PropertyMethod} matches the specified lambda dynamic invocation.
         * 
         * @param name
         * @param descriptor
         * @return {@code boolean}
         */
        boolean matchesLambda(String name, String descriptor) {
            return method.getName().equals(name)
                && Type.getMethodDescriptor(Type.getType(method.getDeclaringClass())).equals(descriptor);
        }

        /**
         * Get the substring preceding the capitalized property name.
         * 
         * @param methodName
         * @param methodType
         * @return {@link String}
         */
        abstract String prefixFrom(String methodName, Type methodType);

        /**
         * Calculate the property name from the specified method handle.
         * 
         * @param methodHandle
         * @return {@link String}, {@code null} if N/A
         */
        String propertyFrom(Handle methodHandle) {
            return Optional.ofNullable(prefixFrom(methodHandle.getName(), Type.getType(methodHandle.getDesc())))
                .map(p -> methodHandle.getName().substring(p.length())).map(Introspector::decapitalize).orElse(null);
        }
    }

    /**
     * {@link MethodVisitor} to wrap property method references.
     */
    class PropertyMethodReferenceWrapper extends GeneratorAdapter {

        /**
         * Create a new {@link PropertyMethodReferenceWrapper} instance.
         * 
         * @param mv
         * @param access
         * @param name
         * @param desc
         */
        PropertyMethodReferenceWrapper(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM6, mv, access, name, desc);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
            Object... bootstrapMethodArguments) {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            if (METAFACTORY_HANDLE.equals(bootstrapMethodHandle) && ClassUtils
                .isAssignable(ClassUtils.toClass(bootstrapMethodArguments), Type.class, Handle.class, Type.class)) {
                final Optional<PropertyMethod> propertyMethod =
                    Stream.of(PropertyMethod.values()).filter(m -> m.matchesLambda(name, descriptor)).findFirst();
                if (propertyMethod.isPresent()) {
                    final PropertyMethod pm = propertyMethod.get();
                    final String property = pm.propertyFrom((Handle) bootstrapMethodArguments[1]);
                    if (StringUtils.isNotBlank(property)) {
                        final Type wrapper = Type.getType(pm.ctor.getDeclaringClass());
                        // stack contains lambda; new -> lambda, wrapper
                        newInstance(wrapper);
                        // dupx1 -> wrapper, lambda, wrapper
                        dupX1();
                        // swap -> wrapper, wrapper, lambda
                        swap();
                        // push -> wrapper, wrapper, lambda, property
                        push(property);
                        // swap -> wrapper, wrapper, property, lambda
                        swap();
                        // invoke ctor (special) -> wrapper
                        invokeConstructor(wrapper, org.objectweb.asm.commons.Method.getMethod(pm.ctor));
                        // move along...
                        modified = true;
                    }
                }
            }
        }
    }

    private static final Handle METAFACTORY_HANDLE;

    static {
        final Method metafactory;
        try {
            metafactory = LambdaMetafactory.class.getMethod("metafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, MethodType.class, MethodHandle.class, MethodType.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        METAFACTORY_HANDLE = new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(LambdaMetafactory.class),
            metafactory.getName(), Type.getMethodDescriptor(metafactory), LambdaMetafactory.class.isInterface());
    }

    private final WeaveEnvironment env;
    private String className;
    private boolean enabled = true;
    private boolean modified;

    /**
     * Create a new {@link PropertyMethodWrappingClassVisitor} instance.
     * 
     * @param next
     */
    PropertyMethodWrappingClassVisitor(WeaveEnvironment env, ClassVisitor next) {
        super(Opcodes.ASM6, next);
        this.env = Validate.notNull(env, "env");
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (PropertyMethodWeaver.MARKER_ANNOTATION.getDescriptor().equals(descriptor)) {
            // already processed; ignore:
            enabled = false;
            env.warn("Detected @%s; skipping %s", WovenPropertyMethods.class.getSimpleName(), className);
        }
        return super.visitAnnotation(descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
        String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        return enabled ? new PropertyMethodReferenceWrapper(mv, access, name, descriptor) : mv;
    }

    /**
     * Learn whether the class was modified.
     * 
     * @return {@code boolean}
     */
    public boolean isModified() {
        return modified;
    }
}
