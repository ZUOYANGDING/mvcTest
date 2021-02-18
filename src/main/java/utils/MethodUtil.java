package utils;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodUtil {
    /**
     * get params name list from methods
     * @param clazz
     * @param method
     * @return
     * @throws IOException
     */
    public static List<String> getMethodParamNames(Class<?> clazz, Method method) throws IOException {
        try (InputStream is = clazz.getResourceAsStream("/" + clazz.getName().replace(".", "/") + ".class")) {
            return getMethodParamNames(is, method);
        }
    }

    public static List<String> getMethodParamNames(InputStream is, Method method) throws IOException {
        try (InputStream is_1=is) {
            return getParamNames(is_1,
                    new EnclosedMetaData(method.getName(), Type.getMethodDescriptor(method), method.getParameterTypes().length));
        }
    }

    /**
     * get constructors' name list
     * @param clazz
     * @param constructor
     * @return
     */
    public static List<String> getConstructorParamNames(Class<?> clazz, Constructor<?> constructor) throws IOException {
        try (InputStream is=clazz.getResourceAsStream("/" + clazz.getName().replace(".", "/") + ".class")) {
            return getConstructorParamNames(is, constructor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<String> getConstructorParamNames(InputStream is, Constructor<?> constructor) throws IOException {
        try (InputStream is_1=is) {
            return getParamNames(is_1,
                    new EnclosedMetaData(constructor.getName(), Type.getConstructorDescriptor(constructor),
                    constructor.getParameterTypes().length));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<String> getParamNames(InputStream is, EnclosedMetaData em) throws IOException {
        ClassReader cr = new ClassReader(is);
        ClassNode node = new ClassNode();
        cr.accept(node, ClassReader.EXPAND_FRAMES);

        List<MethodNode> methodNodeList = node.methods;
        List<String> methodNameList = new ArrayList<>();

        for (int i=0; i<methodNodeList.size(); i++) {
            List<LocalVariable> varNames = new ArrayList<>();
            MethodNode methodNode = methodNodeList.get(i);
            if (methodNode.desc.equals(em.desc) && methodNode.name.equals(em.name)) {
                List<LocalVariableNode> localVariableList = methodNode.localVariables;

                for (int j=0; j<localVariableList.size(); j++) {
                    String varName = localVariableList.get(j).name;
                    int index = localVariableList.get(j).index;
                    if (!"this".equals(varName)) {
                        varNames.add(new LocalVariable(index, varName));
                    }
                }
                LocalVariable[] tempArr = varNames.toArray(new LocalVariable[varNames.size()]);
                Arrays.sort(tempArr);
                for (int j=0; j<em.size; j++) {
                    methodNameList.add(tempArr[j].name);
                }
                break;
            }
        }
        return methodNameList;
    }

    static class LocalVariable implements Comparable<LocalVariable> {
        public int index;
        public String name;

        public LocalVariable(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int compareTo(LocalVariable o) {
            return this.index - o.index;
        }
    }

    static class EnclosedMetaData {
        public String name;
        public String desc;
        public int size;

        public EnclosedMetaData(String name, String desc, int size) {
            this.name = name;
            this.desc = desc;
            this.size = size;
        }
    }
}
