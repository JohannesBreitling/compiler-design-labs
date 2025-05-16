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



public class L1AsmRegisterAllocator implements RegisterAllocator {
    // private int id;
    private final Map<Node, Register> registers = new HashMap<>();

    @Override
    public Map<Node, Register> allocateRegisters(IrGraph graph) {
        Set<Node> visited = new HashSet<>();
        Set<Node> freedNodes = new HashSet<>();

        registerSet = new X86RegisterSet();

        visited.add(graph.endBlock());
        scan(graph.endBlock(), visited, freedNodes);
        
        for (Node node : this.registers.keySet()) {
            System.err.println("Node : " + node.toString() + " has register " + this.registers.get(node).toString());
        }
        
        return Map.copyOf(this.registers);
    } 

    private void scan(Node node, Set<Node> visited, Set<Node> freedNodes) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) { // Scan the predecessor if it has not been scanned yet 
                scan(predecessor, visited, freedNodes);
            }
        }

        // At this point we can gurantee that either there is no predecessor, or the predecessors already have been scanned yet
        if (needsRegister(node)) {
            // Get a register that is currently not in use
            // Utilize that the IR is SSA which means that at a node, only the registers from the predecessors of the node are live 
            this.registers.put(node, registerSet.reserveRegister());
        }

        // Free the register used for the nodes before
        for (Node predecessor : node.predecessors()) {
            if (freedNodes.add(predecessor) && needsRegister(predecessor)) {
                this.registerSet.freeRegister(this.registers.get(predecessor));
            }
        }
    }

    private static boolean needsRegister(Node node) {
        return !(node instanceof ProjNode || node instanceof StartNode || node instanceof Block || node instanceof ReturnNode);
    }

    private X86RegisterSet registerSet;


} 