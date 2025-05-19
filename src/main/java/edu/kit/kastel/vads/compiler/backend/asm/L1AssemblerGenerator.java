package edu.kit.kastel.vads.compiler.backend.asm;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.vads.compiler.backend.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class L1AssemblerGenerator implements CodeGenerator {

    @Override
    public String generateCode(List<IrGraph> program) {
        StringBuilder resultingProgram = initAssembly();

        for (IrGraph graph : program) {
            generateCodeForGraph(graph, resultingProgram);
        } 

        return resultingProgram.toString();
    }


    private StringBuilder initAssembly() {
        StringBuilder initial = new StringBuilder();
        
        initial
            .append(".global main\n")
            .append(".global _main\n")
            .append(".global _overflow\n")
            .append("\n")
            .append(".text\n\n")
            .append("main:\n")
            .append("  call _main\n")
            .append("  movq %rax, %rdi\n")
            .append("  movq $0x3C, %rax\n")
            .append("  syscall\n\n")
            .append("_overflow:\n")
            .append("  mov $0, %ebx\n")
            .append("  div %ebx\n\n")
            // .append("  mov $37, %eax\n")
            // .append("  mov $0, %ebx\n")
            // .append("  mov $8, %ecx\n")
            // .append("  int $0x80\n")
            // .append("  syscall\n\n")
            .append("\n")
            .append("_main:\n");
        
        
        return initial;
    }

    private void generateCodeForGraph(IrGraph graph, StringBuilder result) {            
        // System.out.println(GraphVizPrinter.print(graph));
    
        // Allocate the registers
        L1AsmRegisterAllocator registerAllocator = new L1AsmRegisterAllocator();
        registers = registerAllocator.allocateRegisters(graph);

        // Perform the instruction selection and emit the code
        Set<Node> visited = new HashSet<>();
        scan(graph.endBlock(), visited, result);
    }

    private void scan(Node node, Set<Node> visited, StringBuilder result) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, result);
            }
        }

        // Generate code for the node
        switch (node) {
            case AddNode add -> simpleBinaryOperation(result, add, "ADD");
            case SubNode sub -> simpleBinaryOperation(result, sub, "SUB");
            case MulNode mul -> simpleBinaryOperation(result, mul, "IMUL");
            
            case DivNode div -> divOperation(result, div);
            case ModNode mod -> divOperation(result, mod);
            
            case ReturnNode r -> returnOp(result, r);
            
            case ConstIntNode c -> constInt(result, c);
            
            case Phi _ -> throw new UnsupportedOperationException("phi");
            
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
    }

    private void simpleBinaryOperation(StringBuilder result, BinaryOperationNode node, String opcode) {
        // First move the first argument into the target register
        result.repeat(" ", 2)
            .append("MOV ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(", ")
            .append(registers.get(node))
            .append("\n");
        
        // Perform the operation
        result.repeat(" ", 2)
            .append(opcode)
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)))
            .append(", ")
            .append(registers.get(node))
            .append("\n");

        // // Add overflow if the operation is a mutliplication
        // if (node instanceof MulNode)
        //     result.append("  JO _overflow\n");
        
    }

    private void divOperation(StringBuilder result, BinaryOperationNode node) {
        assert(node instanceof DivNode || node instanceof ModNode);
        
        // Move the dividend to %eax
        result
            .repeat(" ", 2)
            .append("MOV ")
            .append(this.registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(", %eax\n");

        // // Clear the %edx
        // result
        //     .append("  XOR %edx, %edx\n");
        
        result.append("  cdq\n");

        // Divide by the divisor
        result
            .repeat(" ", 2)
            .append("IDIV ")
            .append(this.registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)))
            .append("\n");
        
        // result.append("  JO _overflow\n");

        
        // Get the register to return the result into
        String resultRegister = this.registers.get(node).toString();
        if (node instanceof DivNode) {
            // Return the result of the division (already in %eax)
            result.append("  MOV %eax, ");
        } else {
            // Return the remainder of the division
            result.append("  MOV %edx, ");
        }

        result.append(resultRegister)
            .append("\n");
    }

    private void constInt(StringBuilder result, ConstIntNode node) {
        // Move the constant to the assigned register
        result
            .repeat(" ", 2)
            .append("MOV $")
            .append(node.value())
            .append(", ")
            .append(this.registers.get(node))
            .append("\n");
    }

    private void returnOp(StringBuilder result, ReturnNode r) {
        result.append("  MOV ")
            .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)))
            .append(", %eax\n");

        result.append("  RET\n");
    }

    private Map<Node, Register> registers;
}








    
    

    //     switch (node) {
    
    //         case AddNode add -> binary(builder, registers, add, "add");
    //         case SubNode sub -> binary(builder, registers, sub, "sub");
    //         case MulNode mul -> binary(builder, registers, mul, "mul");
    //         case DivNode div -> binary(builder, registers, div, "div");
    //         case ModNode mod -> binary(builder, registers, mod, "mod");
    //         case ReturnNode r -> builder.repeat(" ", 2).append("ret ")
    //             .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)));
    //         case ConstIntNode c -> builder.repeat(" ", 2)
    //             .append(registers.get(c))
    //             .append(" = const ")
    //             .append(c.value());
    //         case Phi _ -> throw new UnsupportedOperationException("phi");
    //         case Block _, ProjNode _, StartNode _ -> {
    //             // do nothing, skip line break
    //             return;
    //         }
    //     }
    //     builder.append("\n");
    // }
