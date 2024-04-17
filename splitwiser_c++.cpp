#include <iostream>
#include <vector>
#include <deque>
#include <algorithm>
#include <limits>
#include <string>
#include <unordered_set>

using namespace std;

const long long INF = numeric_limits<long long>::max() / 2;

struct Edge {
    int from, to;
    long long flow, capacity;
    Edge* residual;

    Edge(int from, int to, long long capacity) : from(from), to(to), flow(0), capacity(capacity), residual(nullptr) {}
    bool is_residual() { return capacity == 0; }
    long long remaining_capacity() { return capacity - flow; }
    void augment(long long bottle_neck) {
        flow += bottle_neck;
        residual->flow -= bottle_neck;
    }
};

class FlowSolver {
protected:
    int n, s, t;
    long long max_flow;
    bool* min_cut;
    vector<vector<Edge*> > graph;
    string* vertex_labels;
    vector<Edge*> edges;
    int visited_token = 1;
    int* visited_array; // Changed variable name to avoid conflict
    bool solved;

public:
    FlowSolver(int n, string* vertex_labels) : n(n), vertex_labels(vertex_labels), solved(false) {
        initialize_graph();
        assign_labels_to_vertices(vertex_labels);
        min_cut = new bool[n]();
        visited_array = new int[n](); // Changed variable name to avoid conflict
    }

    void initialize_graph() {
        graph.resize(n);
    }

    void assign_labels_to_vertices(string* vertex_labels) {
        this->vertex_labels = vertex_labels;
    }

    void add_edges(vector<Edge*>& edges) {
        for (Edge* edge : edges) {
            add_edge(edge->from, edge->to, edge->capacity);
        }
    }

    void add_edge(int from, int to, long long capacity) {
        Edge* e1 = new Edge(from, to, capacity);
        Edge* e2 = new Edge(to, from, 0);
        e1->residual = e2;
        e2->residual = e1;
        graph[from].push_back(e1);
        graph[to].push_back(e2);
        this->edges.push_back(e1);
    }

    void visit(int i) {
        visited_array[i] = visited_token; // Changed variable name to avoid conflict
    }

    bool is_visited(int i) {
        return visited_array[i] == visited_token; // Changed variable name to avoid conflict
    }

    void mark_all_nodes_as_unvisited() {
        visited_token++;
    }

    vector<vector<Edge*> >& get_graph() {
        execute();
        return graph;
    }

    vector<Edge*>& get_edges() {
        return edges;
    }

    long long get_max_flow() {
        execute();
        return max_flow;
    }

    bool* get_min_cut() {
        execute();
        return min_cut;
    }

    void set_source(int s) {
        this->s = s;
    }

    void set_sink(int t) {
        this->t = t;
    }

    int get_source() {
        return s;
    }

    int get_sink() {
        return t;
    }

    void recompute() {
        solved = false;
    }

    void print_edges() {
        for (Edge* edge : edges) {
            cout << vertex_labels[edge->from] << " owes " << edge->capacity << " rupees to " << vertex_labels[edge->to] << endl;
        }
    }

    virtual void solve() = 0;

protected:
    void execute() {
        if (solved)
            return;
        solved = true;
        solve();
    }
};

class Dinics : public FlowSolver {
private:
    vector<int> level;

public:
    Dinics(int n, string* vertex_labels) : FlowSolver(n, vertex_labels) {
        level.resize(n);
    }

    void solve() override {
        vector<int> next(n);
        while (bfs()) {
            fill(next.begin(), next.end(), 0);
            for (long long f = dfs(s, next, INF); f != 0; f = dfs(s, next, INF)) {
                max_flow += f;
            }
        }
        for (int i = 0; i < n; i++) {
            if (level[i] != -1)
                min_cut[i] = true;
        }
    }

private:
    bool bfs() {
        fill(level.begin(), level.end(), -1);
        level[s] = 0;
        deque<int> q;
        q.push_back(s);
        while (!q.empty()) {
            int node = q.front();
            q.pop_front();
            for (Edge* edge : graph[node]) {
                long long cap = edge->remaining_capacity();
                if (cap > 0 && level[edge->to] == -1) {
                    level[edge->to] = level[node] + 1;
                    q.push_back(edge->to);
                }
            }
        }
        return level[t] != -1;
    }

    long long dfs(int at, vector<int>& next, long long flow) {
        if (at == t)
            return flow;
        for (; next[at] < graph[at].size(); next[at]++) {
            Edge* edge = graph[at][next[at]];
            long long cap = edge->remaining_capacity();
            if (cap > 0 && level[edge->to] == level[at] + 1) {
                long long bottle_neck = dfs(edge->to, next, min(flow, cap));
                if (bottle_neck > 0) {
                    edge->augment(bottle_neck);
                    return bottle_neck;
                }
            }
        }
        return 0;
    }
};

int main() {
    cout << "Enter number of people in the group" << endl;
    int n;
    cin >> n;
    cin.ignore();
    string* group = new string[n];
    cout << "Enter the names of people" << endl;
    for (int i = 0; i < n; i++) {
        getline(cin, group[i]);
    }
    Dinics* solver = new Dinics(n, group);
    vector<Edge*> edges = solver->get_edges();
    int yesn = 1;
    int edge1, edge2, weight;
    while (true) {
        cout << "Enter want to add edge yes/no" << endl;
        cin >> yesn;
        if (yesn == 1) {
            cout << "Enter edge 1:" << endl;
            cin >> edge1;
            cout << "Enter edge 2:" << endl;
            cin >> edge2;
            cout << "Enter weight:" << endl;
            cin >> weight;
            solver->add_edge(edge1, edge2, weight);
        } else {
            break;
        }
    }
    delete solver;
    delete[] group;
    return 0;
}
