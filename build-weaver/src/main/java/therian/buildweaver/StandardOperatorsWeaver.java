package therian.buildweaver;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.weaver.model.ScanRequest;
import org.apache.commons.weaver.model.ScanResult;
import org.apache.commons.weaver.model.Scanner;
import org.apache.commons.weaver.model.WeavableClass;
import org.apache.commons.weaver.model.WeavableField;
import org.apache.commons.weaver.model.WeavableMethod;
import org.apache.commons.weaver.model.WeaveEnvironment;
import org.apache.commons.weaver.model.WeaveInterest;
import org.apache.commons.weaver.spi.Weaver;
import org.kohsuke.MetaInfServices;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@MetaInfServices
public class StandardOperatorsWeaver implements Weaver {
    public static final String TARGET_CLASSNAME = "therian/Operators";
    public static final String STATIC_FIELD_NAME = "_STANDARD_OPERATORS";
    public static final String OPERATOR_TYPE_DESCRIPTOR = "Ltherian/Operator;";
    public static final String STATIC_FIELD_DESCRIPTOR = "[" + OPERATOR_TYPE_DESCRIPTOR;
    public static final String TARGET_METHOD_NAME = "standard";
    public static final String TARGET_METHOD_DESCRIPTOR = "()[" + OPERATOR_TYPE_DESCRIPTOR;
    public static final String NOTES_RESOURCE = "META-INF/therian/standardOperators.txt";

    private static final String METHOD_LOG = TARGET_CLASSNAME + '#' + TARGET_METHOD_NAME + TARGET_METHOD_DESCRIPTOR;

    private static final Type OPERATOR_TYPE = Type.getType(OPERATOR_TYPE_DESCRIPTOR);

    @Override
    public boolean process(final WeaveEnvironment environment, final Scanner scanner) {
        final ScanResult scanResult =
                scanner.scan(new ScanRequest().add(WeaveInterest.of(StandardOperator.class, ElementType.TYPE))
                    .add(WeaveInterest.of(StandardOperator.class, ElementType.FIELD))
                    .add(WeaveInterest.of(StandardOperator.class, ElementType.METHOD)));

        final File classfile = new File(environment.target, TARGET_CLASSNAME + ".class");
        final ClassReader classReader;
        FileInputStream is = null;
        try {
            is = new FileInputStream(classfile);
            classReader = new ClassReader(IOUtils.toByteArray(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        final ClassWriter cw = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) {
            boolean staticFieldWritten = false;
            boolean clinitDone = false;

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
                if ("<clinit>".equals(name)) {
                    clinitDone = true;
                    return new OperatorsClinitVisitor(scanResult, environment.target, delegate);
                }
                if (TARGET_METHOD_NAME.equals(name) && TARGET_METHOD_DESCRIPTOR.equals(desc)) {
                    // replace the whole body of Operators#standard() by cloning the static Operator array
                    return new MethodVisitor(Opcodes.ASM4) {

                        @Override
                        public void visitCode() {
                            delegate.visitCode();
                        }

                        @Override
                        public void visitMaxs(int maxStack, int maxLocals) {
                            delegate.visitFieldInsn(GETSTATIC, TARGET_CLASSNAME, STATIC_FIELD_NAME,
                                STATIC_FIELD_DESCRIPTOR);
                            delegate.visitMethodInsn(INVOKEVIRTUAL, STATIC_FIELD_DESCRIPTOR, "clone",
                                    "()Ljava/lang/Object;");
                            delegate.visitTypeInsn(CHECKCAST, STATIC_FIELD_DESCRIPTOR);
                            delegate.visitInsn(ARETURN);
                            delegate.visitMaxs(maxStack, maxLocals);
                        }
                    };
                }
                return delegate;
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                if (STATIC_FIELD_NAME.equals(name)) {
                    staticFieldWritten = true;
                }
                return super.visitField(access, name, desc, signature, value);
            }

            @Override
            public void visitEnd() {
                if (!staticFieldWritten) {
                    visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, STATIC_FIELD_NAME, STATIC_FIELD_DESCRIPTOR,
                        new StringBuilder(STATIC_FIELD_DESCRIPTOR).insert(STATIC_FIELD_DESCRIPTOR.length() - 1, "<*>")
                        .toString(), null);
                }
                if (!clinitDone) {
                    final MethodVisitor mv = visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                    mv.visitCode();
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }

                super.visitEnd();
            }
        };

        classReader.accept(cv, 0);

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(classfile);
            IOUtils.write(cw.toByteArray(), os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(os);
        }
        Logger.getLogger(getClass().getName()).info("Wove " + METHOD_LOG);
        return true;
    }

    private class OperatorsClinitVisitor extends MethodVisitor {
        final ScanResult scanResult;
        final File target;
        final MutableInt localVariableCount = new MutableInt();

        OperatorsClinitVisitor(ScanResult scanResult, File target, MethodVisitor mv) {
            super(Opcodes.ASM4, mv);
            this.scanResult = scanResult;
            this.target = target;
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            localVariableCount.increment();
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                final StringWriter sw = new StringWriter();
                final PrintWriter w = new PrintWriter(sw);
                final Type list = Type.getType(List.class);
                final Type arrayList = Type.getType(ArrayList.class);
                Label pre = new Label();
                mv.visitLabel(pre);
                mv.visitTypeInsn(NEW, arrayList.getInternalName());
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, arrayList.getInternalName(), "<init>", "()V");
                final int standardOperatorsList = localVariableCount.intValue();
                mv.visitVarInsn(ASTORE, standardOperatorsList);

                final Label begin = new Label();

                Label nextLabel = begin;

                for (WeavableClass<?> weavableClass : scanResult.getClasses().with(StandardOperator.class)) {
                    final Class<?> cls = weavableClass.getTarget();
                    Validate.isTrue(!(cls.isInterface() || Modifier.isAbstract(cls.getModifiers())),
                        "%s is not concrete", cls);
                    Validate.isTrue(ConstructorUtils.getAccessibleConstructor(cls) != null,
                            "%s does not declare public no-arg constructor");

                    w.printf("new %s()%n", cls.getName());

                    final Type type = toOperatorType(cls);

                    mv.visitLabel(nextLabel);
                    mv.visitVarInsn(ALOAD, standardOperatorsList);
                    mv.visitTypeInsn(NEW, type.getInternalName());
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", "()V");
                    mv.visitMethodInsn(INVOKEINTERFACE, list.getInternalName(), "add", "(Ljava/lang/Object;)Z");
                    mv.visitInsn(POP);
                    nextLabel = new Label();
                }

                for (WeavableMethod<?> weavableMethod : scanResult.getMethods().with(StandardOperator.class)) {
                    final Method method = weavableMethod.getTarget();
                    Validate.isTrue(Modifier.isPublic(method.getModifiers()), "%s is not public", method);
                    Validate.isTrue(Modifier.isStatic(method.getModifiers()), "%s is not static", method);
                    Validate.isTrue(method.getParameterTypes().length == 0, "%s requires arguments", method);
                    toOperatorType(method.getReturnType());

                    w.printf("%s.%s()%n", method.getDeclaringClass().getName(), method.getName());

                    mv.visitLabel(nextLabel);
                    mv.visitVarInsn(ALOAD, standardOperatorsList);
                    mv.visitFieldInsn(INVOKESTATIC, Type.getType(method.getDeclaringClass()).getInternalName(),
                        method.getName(), Type.getType(method).getDescriptor());
                    mv.visitMethodInsn(INVOKEINTERFACE, list.getInternalName(), "add", "(Ljava/lang/Object;)Z");
                    mv.visitInsn(POP);
                    nextLabel = new Label();
                }

                for (WeavableField<?> weavableField : scanResult.getFields().with(StandardOperator.class)) {
                    final Field field = weavableField.getTarget();
                    Validate.isTrue(Modifier.isPublic(field.getModifiers()), "%s is not public", field);
                    Validate.isTrue(Modifier.isStatic(field.getModifiers()), "%s is not static", field);
                    Validate.isTrue(Modifier.isFinal(field.getModifiers()), "%s is not final", field);
                    toOperatorType(field.getType());

                    w.printf("%s.%s%n", field.getDeclaringClass().getName(), field.getName());

                    mv.visitLabel(nextLabel);
                    mv.visitVarInsn(ALOAD, standardOperatorsList);
                    mv.visitFieldInsn(GETSTATIC, Type.getType(field.getDeclaringClass()).getInternalName(),
                        field.getName(), Type.getType(field.getType()).getDescriptor());
                    mv.visitMethodInsn(INVOKEINTERFACE, list.getInternalName(), "add", "(Ljava/lang/Object;)Z");
                    mv.visitInsn(POP);
                    nextLabel = new Label();
                }

                mv.visitLabel(nextLabel);
                mv.visitVarInsn(ALOAD, standardOperatorsList);
                mv.visitVarInsn(ALOAD, standardOperatorsList);
                mv.visitMethodInsn(INVOKEINTERFACE, list.getInternalName(), "size", "()I");
                mv.visitTypeInsn(ANEWARRAY, OPERATOR_TYPE.getInternalName());
                mv.visitMethodInsn(INVOKEINTERFACE, list.getInternalName(), "toArray",
                        "([Ljava/lang/Object;)[Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, STATIC_FIELD_DESCRIPTOR);
                mv.visitFieldInsn(PUTSTATIC, TARGET_CLASSNAME, STATIC_FIELD_NAME, STATIC_FIELD_DESCRIPTOR);

                final Label end = new Label();
                mv.visitLabel(end);
                mv.visitInsn(RETURN);

                mv.visitLocalVariable("_result", list.getDescriptor(),
                    parameterize(list.getDescriptor(), parameterize(OPERATOR_TYPE_DESCRIPTOR, "*")), begin, end,
                    standardOperatorsList);

                final File dataFile = new File(target, NOTES_RESOURCE);
                if (!dataFile.getParentFile().isDirectory()) {
                    dataFile.getParentFile().mkdirs();
                }
                FileOutputStream dataFileOut = null;
                try {
                    dataFileOut = new FileOutputStream(dataFile);
                    IOUtils.write(sw.toString(), dataFileOut);
                } catch (IOException e) {
                } finally {
                    IOUtils.closeQuietly(dataFileOut);
                }
            } else {
                super.visitInsn(opcode);
            }
        }
    }

    private static Type toOperatorType(Class<?> cls) {
        final Type type = Type.getType(cls);
        if (type.equals(OPERATOR_TYPE)) {
            return type;
        }
        for (Class<?> iface : ClassUtils.getAllInterfaces(cls)) {
            if (Type.getType(iface).equals(OPERATOR_TYPE)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("%s is not assignable to %s", type, OPERATOR_TYPE));
    }

    private static String parameterize(String desc, String... params) {
        final int ins = desc.length() - 1;
        final StringBuilder buf = new StringBuilder(desc).insert(ins, '>');
        int p = params.length - 1;
        buf.insert(ins, params[p]);
        while (--p >= 0) {
            buf.insert(ins, ", ").insert(ins, params[p]);
        }
        return buf.insert(ins, '<').toString();
    }

}
