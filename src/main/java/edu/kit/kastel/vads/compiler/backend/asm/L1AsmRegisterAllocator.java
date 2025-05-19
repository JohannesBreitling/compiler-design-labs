package edu.kit.kastel.vads.compiler.backend.asm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter;



public class L1AsmRegisterAllocator implements RegisterAllocator {

    @Override
    public Map<Node, Register> allocateRegisters(IrGraph graph) {
        Set<Node> visited = new HashSet<>();
        Set<Node> assigned = new HashSet<>();
        Set<Node> freed = new HashSet<>();

        System.out.println("Print the graph: ");
        System.out.println(GraphVizPrinter.print(graph));

        this.graph = graph;
        registerSet = new X86RegisterSet();
        this.registers = new HashMap<>();

        visited.add(graph.endBlock());
        scan(graph.endBlock(), visited, assigned, freed);
        
        return Map.copyOf(this.registers);
    }

    private void scan(Node node, Set<Node> visited, Set<Node> assigned, Set<Node> freed) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) { // Scan the predecessor if it has not been scanned yet 
                scan(predecessor, visited, assigned, freed);
            }
        }

        if (!needsRegister(node))
            return;

        // At this point we can gurantee that either there is no predecessor, or the predecessors already have been scanned yet
        
        // Get a register that is currently not in use
        // Utilize that the IR is SSA which means that at a node, only the registers from the predecessors of the node are live 
        this.registers.put(node, registerSet.reserveRegister());
        assigned.add(node);

        // Free the predecessor nodes that will not be used any more
        for (Node predecessor : node.predecessors()) {
            freeNode(predecessor, assigned, freed);
        }
    }

    private void freeNode(Node node, Set<Node> assigned, Set<Node> freed) {
        if (!needsRegister(node))
            return;

        Set<Node> successors = graph.successors(node);
        for (Node successor : successors) {
            if (!assigned.contains(successor) || freed.contains(successor))
               return;
        }

        freed.add(node);
        this.registerSet.freeRegister(this.registers.get(node));
    }

    private static boolean needsRegister(Node node) {
        return !(node instanceof ProjNode || node instanceof StartNode || node instanceof Block || node instanceof ReturnNode);
    }

    private X86RegisterSet registerSet;
    private Map<Node, Register> registers;
    private IrGraph graph;

} 