package org.antlr.v4.runtime.programanalysis.cfg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class GraphBuilder<N extends Node> {

  private AbstractParseTreeVisitor<N> visitor;

  public Graph<N> build() {

	return doBuild(null);
  }

  private Graph<N> doBuild(ParserRuleContext context) {
	Graph<N> graph = new Graph<>(ArrayListMultimap.create(), Collections.emptySet(), Collections.emptySet());
	List<BasicBlock> basicBlocks = new LinkedList<>();

	return graph;
  }
}
