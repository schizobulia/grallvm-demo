package org.example;


import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.meta.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;


public class Main {
    static MetaAccessProvider metaAccessProvider =  HotSpotJVMCIRuntime.runtime().getHostJVMCIBackend().getMetaAccess();
    static Graph graph = new Graph();

    static Queue<Object> frame = new ArrayDeque<Object>();
    static ArrayList<Object> frameIndex = new ArrayList<Object>();

    public static void main(String[] args) throws ClassNotFoundException {
        String root = "../../../../../";
        String mainClassName = "HelloWorld";
        Class<?> mainClass = getMainClass(root, mainClassName);
        Method mainMethod = getMethod(mainClass, "main");
        ResolvedJavaMethod mainResolvedJavaMethod = getResolvedJavaMethod(mainMethod);
        ergodicByteCode(mainResolvedJavaMethod);
        graph.showGrap();

        GraalStyleTraversal traversal = new GraalStyleTraversal(graph);
        traversal.traverseIR();
        HIR hir = convertToHIR(graph);

        System.out.println(hir);


//        byte[] mainMethodCode = getMethodCode(mainMethod);
//        printByteCode(mainMethodCode);
//        printByteCode(mainMethodCode);
//        NativeImageSystemClassLoader nativeImageSystemClassLoader = NativeImageSystemClassLoader.singleton();
//        NativeImageClassLoaderSupport nativeImageClassLoaderSupport = new NativeImageClassLoaderSupport(nativeImageSystemClassLoader.defaultSystemClassLoader, classpath, modulepath);
//        Object o = account.newInstance();
//        ClassLoader classLoader = o.getClass().getClassLoader();
//        System.out.println("加载当前类的类加载器为：" + classLoader);
//        System.out.println("父类加载器为：" + classLoader.getParent());
//        try (Context context = Context.create()) {
//            context.eval("js", "console.log('Hello, world!')");
//        }
    }


    public static HIR convertToHIR(Graph graph) {
        HIR hir = new HIR();
        HIR.HIRBlock block = hir.new HIRBlock();

        for (Graph.Node node : graph.vertexList) {
            switch (node.type) {
                case Graph.Relation.IADD:
                    // 对于加法操作，我们需要两个操作数
                    Graph.Edge edge1 = graph.edges.get(0); // 假设第一个边是第一个操作数
                    Graph.Edge edge2 = graph.edges.get(1); // 假设第二个边是第二个操作数
                    HIR.IRBinaryExpression addExpr = hir.new IRBinaryExpression(Graph.Relation.IADD, graph.vertexList.get(edge1.v1), graph.vertexList.get(edge2.v1));
                    block.expressions.add(addExpr);
                    break;
                case Graph.Relation.CALL:
                    // 对于函数调用，我们只需要函数节点
                    HIR.IRFunctionCall callExpr = hir.new IRFunctionCall(Graph.Relation.CALL, node);
                    block.expressions.add(callExpr);
                    break;
                // 其他节点类型的处理...
            }
        }

        // 构建HIR控制流，如循环、分支等
        // ...

        return hir;
    }


    static void ergodicByteCode(ResolvedJavaMethod method) {
        int voidIndex = insertFunNode(method);
        ConstantPool cp = method.getConstantPool();
        byte[] code = method.getCode();
        BytecodeStream stream = new BytecodeStream(code);
        int previousOpCode = -1;
        int previousOpIndex = -1;
        int key = 0;
        while (stream.endBCI() > key) {
            int opcode = stream.currentBC();
            if (opcode == Bytecodes.BIPUSH){
                frame.add(stream.readByte());
            } else if (opcode == Bytecodes.INVOKESTATIC) {
                int cpi = stream.readCPI();
                ResolvedJavaMethod javaMethod = (ResolvedJavaMethod) cp.lookupMethod(cpi, opcode);
                graph.insertEdges(voidIndex, graph.getNumOfVertex(), Graph.Relation.CALL);
                ergodicByteCode(javaMethod);
            } else if (opcode == Bytecodes.ILOAD_0 || opcode == Bytecodes.ILOAD_1) {
                int iloadIndex = insertIloadNode(voidIndex, Integer.parseInt(frame.poll().toString()));
                frameIndex.add(iloadIndex);
            } else if (opcode == Bytecodes.IADD) {
                int addIndex = graph.insertVertex(new Graph.Node(Graph.Relation.IADD, Graph.Relation.IADD, Graph.Relation.IADD));
                for (int i = 0; i < frameIndex.size(); i++) {
                    int fi = Integer.parseInt(frameIndex.get(i).toString());
                    graph.insertEdges(fi, addIndex, Graph.Relation.IADD);
                }
                frameIndex = new ArrayList<Object>();
                previousOpIndex = addIndex;
            } else if (opcode == Bytecodes.IRETURN) {
                int returnIndex = graph.insertVertex(new Graph.Node(Graph.Relation.RETURN, Graph.Relation.RETURN, Graph.Relation.RETURN));
                if (previousOpCode == Bytecodes.IADD) {
                    graph.insertEdges(previousOpIndex, returnIndex, Graph.Relation.RETURNVAL);
                }
                graph.insertEdges(voidIndex, returnIndex, Graph.Relation.RETURN);
            } else if (opcode == Bytecodes.END && previousOpCode != Bytecodes.END){
                int returnIndex = graph.insertVertex(new Graph.Node(Graph.Relation.RETURN, Graph.Relation.RETURN, Graph.Relation.RETURN));
                graph.insertEdges(voidIndex, returnIndex, Graph.Relation.RETURN);
            } else if (opcode == Bytecodes.INVOKEVIRTUAL) {
                int cpi = stream.readCPI();
                JavaType javaType = cp.lookupReferencedType(cpi, opcode);
                System.out.println(javaType.getName());
            }
            previousOpCode = opcode;
            key++;
            stream.next();
        }
    }

    static int insertIloadNode(int voidIndex, int val) {
        int i = graph.insertVertex(new Graph.Node("number", Graph.Relation.NUMBER, val));
        graph.insertEdges(voidIndex, i, Graph.Relation.ARG);
        return i;
    }

    static int insertFunNode(ResolvedJavaMethod method) {
        int startIndex = graph.insertVertex(new Graph.Node(Graph.Relation.START, Graph.Relation.START, ""));
        int voidIndex = graph.insertVertex(new Graph.Node(method.getName(), Graph.Relation.VOID, method));
        graph.insertEdges(startIndex, voidIndex, Graph.Relation.START);
        return voidIndex;
    }

    static Class<?> getMainClass(String dir, String name) throws ClassNotFoundException {
        MyClassLoader fileReadClassLoader = new MyClassLoader(dir);
        return fileReadClassLoader.loadClass(name);
    }

    static Method getMethod(Class<?> account, String name) {
        Method[] methods = account.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 将Method转换为ResolvedJavaMethod，方便获取字节码
     * @param method
     * @return
     */
    static ResolvedJavaMethod getResolvedJavaMethod(Method method) {
        return metaAccessProvider.lookupJavaMethod(method);
    }

    static void printByteCode(byte[] code) {
        for (int i = 0; i < code.length; i++) {
            System.out.println(Bytecodes.operator(code[i]));
        }
    }


    static class MyClassLoader extends ClassLoader {
        private String dir;
        public byte[] byteCode;

        public MyClassLoader(String dir) {
            this.dir = dir;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // 读取class
                this.byteCode = getClassBytes(name);
                // 将二进制class转换为class对象
                Class<?> c = this.defineClass(null, byteCode, 0, byteCode.length);
                return c;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.findClass(name);
        }

        private byte[] getClassBytes(String name) throws Exception {
            FileInputStream fis = new FileInputStream(new File(this.dir + File.separator + name + ".class"));
            FileChannel fc = fis.getChannel();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WritableByteChannel wbc = Channels.newChannel(baos);
            ByteBuffer by = ByteBuffer.allocate(1024);
            while (true) {
                int i = fc.read(by);
                if (i == 0 || i == -1)
                    break;
                by.flip();
                wbc.write(by);
                by.clear();
            }
            fis.close();
            return baos.toByteArray();
        }
    }
}