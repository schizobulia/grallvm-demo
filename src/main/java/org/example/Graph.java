package org.example;

import java.util.ArrayList;


/**
 * https://www.bilibili.com/video/BV1E4411H73v
 */
class Graph {
    public ArrayList<Node> vertexList; //存储定点
    ArrayList<Edge> edges;   // 存储图对应的邻结矩阵
    private int numOfEges; //边的数量

    public Graph() {
        edges = new ArrayList<Edge>();
        vertexList = new ArrayList<Node>();
        numOfEges = 0;
    }

    public int insertVertex(Node node) {
        vertexList.add(node);
        return vertexList.size() - 1;
    }

    /**
     *
     * @param v1 第一个顶点所在点的下标
     * @param v2 第二个顶点所在点的下标
     * @param weight
     */
    public void insertEdges(int v1, int v2, String weight) {
        edges.add(new Edge(v1, v2, weight));
//        edges.add(new Edge(v2, v1, weight));
        numOfEges++;
    }

    public int getNumOfEges() {
        return numOfEges;
    }

    public int getNumOfVertex() {
        return vertexList.size();
    }

    public Node getValueByIndex(int i) {
        return vertexList.get(i);
    }

    public String getWeight(int v1, int v2) {
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (v1 == e.v1 && v2 == e.v2) {
                return e.weight;
            }
        }
        return "";
    }

    public void showGrap() {
        for (int i = 0; i < vertexList.size(); i++) {
            for (int j = 0; j < vertexList.size(); j++) {
                int tag = 0;
                for (int k = 0; k < edges.size(); k++) {
                    Edge edge = edges.get(k);
                    if (edge.v1 == i && edge.v2 == j) {
                        tag = 1;
                        System.out.print(edge.weight);
                        System.out.print(",");
                    }
                }
                if (tag == 0) {
                    System.out.print(0);
                    System.out.print(",");
                }
            }
            System.out.println("");
        }
    }

    static class Node {
        String name;
        String type;
        Object value;
        Node(String name, String type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
    }

    class Relation {
        public static final String IADD = "+";
        public static final String VOID = "fun";
        public static final String NUMBER = "num";
        public static final String ARG = "arg";
        public static final String RETURN = "return";
        public static final String RETURNVAL = "returnVal";
        public static final String START = "start";
        public static final String CALL = "call";
    }

    static class Edge {
        int v1;
        int v2;
        String weight;
        public Edge(int v1, int v2, String weight) {
            this.v1 = v1;
            this.v2 = v2;
            this.weight = weight;
        }
    }
}
