package org.antlr.v4.runtime.programanalysis.cfg;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;

@Getter
public class Graph<N extends Node> {

  private final Multimap<N, N> edges;
  private final Multimap<N, N> revEdges = ArrayListMultimap.create();

  private final Set<N> startNodes;
  private final Set<N> endNodes;

  Graph(Multimap<N, N> edges, Set<N> startNodes, Set<N> endNodes) {
	this.edges = edges;
	this.startNodes = startNodes;
	this.endNodes = endNodes;
  }

  public Collection<N> successors(N node) {
	return edges.get(node);
  }

  public Collection<N> predecessors(N node) {
    Multimaps.invertFrom(edges, revEdges);
	return revEdges.get(node);
  }
}
