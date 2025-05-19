
package edu.kit.kastel.vads.compiler.backend.asm;

import java.util.HashMap;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;



class X86RegisterSet {

    // private HashMap<Node, String> registers;
    private final HashMap<X86Register, Boolean> registersUsed;

    public X86RegisterSet() {
        // Construct the register set with the registers we can use for our computation
        this.registersUsed = new HashMap<>();
        // this.registersUsed.put(new X86Register("%r8d"), false); Using r8 now for spilling
        this.registersUsed.put(new X86Register("%r9d"), false);
        this.registersUsed.put(new X86Register("%r10d"), false);
        this.registersUsed.put(new X86Register("%r11d"), false);
        this.registersUsed.put(new X86Register("%r12d"), false);
        this.registersUsed.put(new X86Register("%r13d"), false);
        this.registersUsed.put(new X86Register("%r14d"), false);
        this.registersUsed.put(new X86Register("%r15d"), false);
    }

    public X86Register reserveRegister() {
        for (var key : registersUsed.keySet()) {
            if (!registersUsed.get(key)) {
                registersUsed.put(key, true);
                return key;
            }
        }
        
        // At this point, no register is available and we need to spill the variable
        maxOffset += 4;
        var register = new X86Register("-" + maxOffset + "(%rsp)", true, maxOffset);
        // usedOffsets.put(currentOffset, true); // TODO: Implement the freeing of stack slots that are spilled
        return register;
    }

    public void freeRegister(Register register) {
        X86Register converted = (X86Register) register;

        if (converted.isSpilled()) return;
        
        registersUsed.put(converted, false);
    }

    public int getMaxOffset() {
        return maxOffset;
    }

    private int maxOffset = 0;
    // private Map<Integer, Boolean> usedOffsets;

}