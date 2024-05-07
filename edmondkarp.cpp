#include <iostream>
#include <climits>
#include <cstring>
#include <queue>
#include <vector>
#include <chrono> // Include chrono library for time measurement

using namespace std;

const int INF = INT_MAX;
const int MAX_V = 100; // Adjust this according to your maximum number of vertices

int capacity[MAX_V][MAX_V];
int parent[MAX_V];

int bfs(int source, int sink, vector<vector<int>> &graph) {
  fill(parent, parent + MAX_V, -1);
  parent[source] = source;
  queue<pair<int, int>> q;
  q.push({source, INF});

  while (!q.empty()) {
    int current = q.front().first;
    int flow = q.front().second;
    q.pop();

    for (int next : graph[current]) {
      if (parent[next] == -1 && capacity[current][next] > 0) {
        parent[next] = current;
        int new_flow = min(flow, capacity[current][next]);
        if (next == sink)
          return new_flow;
        q.push({next, new_flow});
      }
    }
  }
  return 0;
}

int edmondsKarp(int source, int sink, vector<vector<int>> &graph) {
  int max_flow = 0;
  int new_flow;

  while ((new_flow = bfs(source, sink, graph)) > 0) {
    max_flow += new_flow;
    int current = sink;
    while (current != source) {
      int prev = parent[current];
      capacity[prev][current] -= new_flow;
      capacity[current][prev] += new_flow;
      current = prev;
    }
  }
  return max_flow;
}

int main() {
  // Example usage:
  int source = 0;
  int sink = 5;
  vector<vector<int>> graph(MAX_V);

  // Add directed edges with capacities
  // ... (same as before)

  // Create adjacency list representation
  for (int i = 0; i < MAX_V; ++i) {
    for (int j = 0; j < MAX_V; ++j) {
      if (capacity[i][j] > 0) {
        graph[i].push_back(j);
      }
    }
  }

  // Start time measurement
  auto start = chrono::high_resolution_clock::now();

  int max_flow = edmondsKarp(source, sink, graph);

  // End time measurement
  auto end = chrono::high_resolution_clock::now();

  // Calculate execution time in milliseconds
  chrono::duration<double, milli> duration = end - start;

  cout << "Maximum Flow: " << max_flow << endl;
  cout << "Execution Time: " << duration.count() << " milliseconds" << endl;

  return 0;
}
