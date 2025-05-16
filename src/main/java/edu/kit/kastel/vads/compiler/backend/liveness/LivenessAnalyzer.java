package edu.kit.kastel.vads.compiler.backend.liveness;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;


public class LivenessAnalyzer {

    public LivenessAnalyzer(List<IrGraph> graphs) {
        this.graphs = graphs;
    }

    public void analyze() {
        for (IrGraph graph : graphs) {
            analyzeGraph(graph);
        }
    }

    public Map<Node, List<String>> getLivenessDataset() {        
        return null;
    }

    private void analyzeGraph(IrGraph graph) {
        var endBlock = graph.endBlock();
    }

    // private void 

    





    public List<IrGraph> graphs;

    // Map<Integer, String>


    // IrGraph 



}