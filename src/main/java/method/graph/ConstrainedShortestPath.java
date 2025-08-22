package method.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 这个类实现了约束最短路径问题（CSPP）的一个解。
 * 也称为资源约束最短路径问题（SPPRC）。
 * 目标是找到两个节点之间的最短路径，同时确保没有超出资源限制。
 *
 * @author  <a href="https://github.com/DenizAltunkapan">Deniz Altunkapan</a>
 */
public class ConstrainedShortestPath {

    /**
     * 表示使用邻接表的图。
     * 这张图是为约束最短路径问题（CSPP）设计的。
     */
    public static class Graph {

        private List<List<Edge>> adjacencyList;

        public Graph(int numNodes) {
            adjacencyList = new ArrayList<>();
            for (int i = 0; i < numNodes; i++) {
                adjacencyList.add(new ArrayList<>());
            }
        }

        /**
         * 向图形添加一条边。
         * @param from 起始节点
         * @param to 结束节点
         * @param cost 这条边的代价
         * @param resource 遍历边缘所需的资源
         */
        public void addEdge(int from, int to, int cost, int resource) {
            adjacencyList.get(from).add(new Edge(from, to, cost, resource));
        }

        /**
         * Gets the edges that are adjacent to a given node.
         * @param node the node to get the edges for
         * @return the list of edges adjacent to the node
         */
        public List<Edge> getEdges(int node) {
            return adjacencyList.get(node);
        }

        /**
         * Gets the number of nodes in the graph.
         * @return the number of nodes
         */
        public int getNumNodes() {
            return adjacencyList.size();
        }

        public record Edge(int from, int to, int cost, int resource) {
        }
    }

    private Graph graph;
    private int maxResource;

    /**
     * 构造具有给定图和最大资源约束的CSPSolver。
     *
     * @param graph       表示问题的图形
     * @param maxResource 允许的最大资源
     */
    public ConstrainedShortestPath(Graph graph, int maxResource) {
        this.graph = graph;
        this.maxResource = maxResource;
    }

    /**
     * 求解CSP，找到从起始节点到目标节点的最短路径
     * 在不超出资源限制的情况下。
     *
     * @param start  起始节点
     * @param target 目标节点
     * @return 在资源约束下到达目标节点的最小成本,
     *         如果不存在有效路径，则为-1
     */
    public int solve(int start, int target) {
        int numNodes = graph.getNumNodes();
        int[][] dp = new int[maxResource + 1][numNodes];

        // Initialize dp table with maximum values
        for (int i = 0; i <= maxResource; i++) {
            Arrays.fill(dp[i], Integer.MAX_VALUE);
        }
        dp[0][start] = 0;

        // 动态规划：迭代资源和节点
        for (int r = 0; r <= maxResource; r++) {
            for (int u = 0; u < numNodes; u++) {
                if (dp[r][u] == Integer.MAX_VALUE) {
                    continue;
                }
                for (Graph.Edge edge : graph.getEdges(u)) {
                    int v = edge.to();
                    int cost = edge.cost();
                    int resource = edge.resource();

                    if (r + resource <= maxResource) {
                        dp[r + resource][v] = Math.min(dp[r + resource][v], dp[r][u] + cost);
                    }
                }
            }
        }

        // 找到到达目标节点的最小代价
        int minCost = Integer.MAX_VALUE;
        for (int r = 0; r <= maxResource; r++) {
            minCost = Math.min(minCost, dp[r][target]);
        }

        return minCost == Integer.MAX_VALUE ? -1 : minCost;
    }
}
