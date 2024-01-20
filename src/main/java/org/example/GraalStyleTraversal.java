package org.example;

public class GraalStyleTraversal {
    private Graph graph;

    public GraalStyleTraversal(Graph graph) {
        this.graph = graph;
    }

    void traverseIR(Graph.Node node, boolean[] visited) {
        // 标记当前节点为已访问
        int v = graph.vertexList.indexOf(node);
        visited[v] = true;
        System.out.println("Visiting node: " + node.name);

        // 遍历邻接节点
        for (int j = 0; j < graph.getNumOfVertex(); j++) {
            if (graph.getWeight(v, j) != "" && !visited[j]) {
                traverseIR(graph.getValueByIndex(j), visited);
            }
            if (graph.getWeight(j, v) != "" && !visited[j]) {
                traverseIR(graph.getValueByIndex(j), visited);
            }
        }
    }

    // 对traverseIR进行一个重载，遍历所有的节点，并进行DFS
    public void traverseIR() {
        // 是否被访问过
        boolean[] visited = new boolean[graph.getNumOfVertex()];
        for (int i = 0; i < graph.getNumOfVertex(); i++) {
            if (!visited[i]) {
                traverseIR(graph.getValueByIndex(i), visited);
            }
        }
    }
}
