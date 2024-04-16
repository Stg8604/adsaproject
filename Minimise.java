import java.util.*;
import static java.lang.Math.min;

public class Minimise {
    private static final long OFFSET = 1000000000L;
    private static Set<Long> visitedEdges;

    public static void main(String[] args) {
        createGraph();
    }

    private static void createGraph() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of people in the group");
        int n=scanner.nextInt();
        scanner.nextLine();
        String[] group=new String[n];
        System.out.println("Enter the names of people");
        for(int i=0;i<n;i++){
            group[i]=scanner.nextLine();
        }
        scanner.close();
        Dinics solver = new Dinics(n, group);
        solver = addAllTransactions(solver);
        System.out.println();
        System.out.println("Minimised Values:");
        System.out.println();
        visitedEdges = new HashSet<>();
        Integer edgePos;

        while ((edgePos = noVisit(solver.getEdges())) != null) {
            solver.recompute();
            Dinics.Edge firstEdge = solver.getEdges().get(edgePos);
            solver.setSource(firstEdge.from);
            solver.setSink(firstEdge.to);
            List<Dinics.Edge>[] residualGraph = solver.getGraph();
            List<Dinics.Edge> newEdges = new ArrayList<>();
            for (List<Dinics.Edge> allEdges : residualGraph) {
                for (Dinics.Edge edge : allEdges) {
                    long remainingFlow = ((edge.flow < 0) ? edge.capacity : (edge.capacity - edge.flow));
                    if (remainingFlow > 0) {
                        newEdges.add(new Dinics.Edge(edge.from, edge.to, remainingFlow));
                    }
                }
            }
            long maxFlow = solver.getMaxFlow();
            int source = solver.getSource();
            int sink = solver.getSink();
            visitedEdges.add(getHashKeyForEdge(source, sink));
            solver = new Dinics(n, group);
            solver.addEdges(newEdges);
            solver.addEdge(source, sink, maxFlow);
        }
        solver.printEdges();
        System.out.println();
    }

    private static Dinics addAllTransactions(Dinics solver) {
        /* 
        solver.addEdge(1, 2, 20);
        solver.addEdge(1, 3, 10);
        solver.addEdge(3, 4, 15);
        solver.addEdge(3, 6, 11);
        solver.addEdge(7, 6, 20);
        solver.addEdge(7, 1, 25);
        solver.addEdge(4, 5, 6);
        solver.addEdge(5, 2, 9);
        solver.addEdge(5, 7, 13);
        */
        int yesn=1;
        int edge1,edge2,weight;
        Scanner scanner=new Scanner(System.in);
        while(true){
            System.out.println("Enter want to add edge yes/no");
            yesn=scanner.nextInt();
            if(yesn==1){
                System.out.println("Enter edge 1:");
                edge1=scanner.nextInt();
                System.out.println("Enter edge 2:");
                edge2=scanner.nextInt();
                System.out.println("Enter weight:");
                weight=scanner.nextInt();
                scanner.close();
                solver.addEdge(edge1,edge2,weight);
            } else{
                break;
            }
        }
        return solver;
    }

    private static Integer noVisit(List<Dinics.Edge> edges) {
        Integer edgePos = null;
        int curEdge = 0;
        for (Dinics.Edge edge : edges) {
            if (!visitedEdges.contains(getHashKeyForEdge(edge.from, edge.to))) {
                edgePos = curEdge;
            }
            curEdge++;
        }
        return edgePos;
    }

    private static Long getHashKeyForEdge(int u, int v) {
        return u * OFFSET + v;
    }
}

class Dinics extends FlowSolver {

    private int[] level;

    public Dinics(int n, String[] vertexLabels) {
        super(n, vertexLabels);
        level = new int[n];
    }

    @Override
    public void solve() {
       
        int[] next = new int[n];

        while (bfs()) {
            Arrays.fill(next, 0);
            for (long f = dfs(s, next, INF); f != 0; f = dfs(s, next, INF)) {
                maxFlow += f;
            }
        }

        for (int i = 0; i < n; i++)
            if (level[i] != -1)
                minCut[i] = true;
    }

    private boolean bfs() {
        Arrays.fill(level, -1);
        level[s] = 0;
        Deque<Integer> q = new ArrayDeque<>(n);
        q.offer(s);
        while (!q.isEmpty()) {
            int node = q.poll();
            for (Edge edge : graph[node]) {
                long cap = edge.remainingCapacity();
                if (cap > 0 && level[edge.to] == -1) {
                    level[edge.to] = level[node] + 1;
                    q.offer(edge.to);
                }
            }
        }
        return level[t] != -1;
    }

    private long dfs(int at, int[] next, long flow) {
        if (at == t)
            return flow;
        final int numEdges = graph[at].size();

        for (; next[at] < numEdges; next[at]++) {
            Edge edge = graph[at].get(next[at]);
            long cap = edge.remainingCapacity();
            if (cap > 0 && level[edge.to] == level[at] + 1) {

                long bottleNeck = dfs(edge.to, next, min(flow, cap));
                if (bottleNeck > 0) {
                    edge.augment(bottleNeck);
                    return bottleNeck;
                }
            }
        }
        return 0;
    }
}

abstract class FlowSolver {

    protected static final long INF = Long.MAX_VALUE / 2;

    public static class Edge {
        public int from, to;
        public String fromLabel, toLabel;
        public Edge residual;
        public long flow, cost;
        public final long capacity, originalCost;

        public Edge(int from, int to, long capacity) {
            this(from, to, capacity, 0 /* unused */);
        }

        public Edge(int from, int to, long capacity, long cost) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.originalCost = this.cost = cost;
        }

        public boolean isResidual() {
            return capacity == 0;
        }

        public long remainingCapacity() {
            return capacity - flow;
        }

        public void augment(long bottleNeck) {
            flow += bottleNeck;
            residual.flow -= bottleNeck;
        }

        public String toString(int s, int t) {
            String u = (from == s) ? "s" : ((from == t) ? "t" : String.valueOf(from));
            String v = (to == s) ? "s" : ((to == t) ? "t" : String.valueOf(to));
            return String.format(
                    "Edge %s -> %s | flow = %d | capacity = %d | is residual: %s",
                    u, v, flow, capacity, isResidual());
        }
    }

    protected int n, s, t;

    protected long maxFlow;
    protected long minCost;

    protected boolean[] minCut;
    protected List<Edge>[] graph;
    protected String[] vertexLabels;
    protected List<Edge> edges;

    private int visitedToken = 1;
    private int[] visited;

    protected boolean solved;

    public FlowSolver(int n, String[] vertexLabels) {
        this.n = n;
        initializeGraph();
        assignLabelsToVertices(vertexLabels);
        minCut = new boolean[n];
        visited = new int[n];
        edges = new ArrayList<>();
    }

    private void initializeGraph() {
        graph = new List[n];
        for (int i = 0; i < n; i++)
            graph[i] = new ArrayList<Edge>();
    }

    private void assignLabelsToVertices(String[] vertexLabels) {
        if (vertexLabels.length != n)
            throw new IllegalArgumentException(String.format("You must pass %s number of labels", n));
        this.vertexLabels = vertexLabels;
    }

    public void addEdges(List<Edge> edges) {
        if (edges == null)
            throw new IllegalArgumentException("Edges cannot be null");
        for (Edge edge : edges) {
            addEdge(edge.from, edge.to, edge.capacity);
        }
    }

    public void addEdge(int from, int to, long capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("Capacity < 0");
        Edge e1 = new Edge(from, to, capacity);
        Edge e2 = new Edge(to, from, 0);
        e1.residual = e2;
        e2.residual = e1;
        graph[from].add(e1);
        graph[to].add(e2);
        edges.add(e1);
    }

    public void addEdge(int from, int to, long capacity, long cost) {
        Edge e1 = new Edge(from, to, capacity, cost);
        Edge e2 = new Edge(to, from, 0, -cost);
        e1.residual = e2;
        e2.residual = e1;
        graph[from].add(e1);
        graph[to].add(e2);
        edges.add(e1);
    }

    public void visit(int i) {
        visited[i] = visitedToken;
    }

    public boolean visited(int i) {
        return visited[i] == visitedToken;
    }

 
    public void markAllNodesAsUnvisited() {
        visitedToken++;
    }

    public List<Edge>[] getGraph() {
        execute();
        return graph;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public long getMaxFlow() {
        execute();
        return maxFlow;
    }

    public long getMinCost() {
        execute();
        return minCost;
    }

    public boolean[] getMinCut() {
        execute();
        return minCut;
    }

    public void setSource(int s) {
        this.s = s;
    }

    public void setSink(int t) {
        this.t = t;
    }

    public int getSource() {
        return s;
    }

    public int getSink() {
        return t;
    }

    public void recompute() {
        solved = false;
    }

    public void printEdges() {
        for (Edge edge : edges) {
            System.out.println(
                    String.format("%s owes %s rupees to %s", vertexLabels[edge.from], edge.capacity, vertexLabels[edge.to]));
        }
    }

    private void execute() {
        if (solved)
            return;
        solved = true;
        solve();
    }

    public abstract void solve();
}