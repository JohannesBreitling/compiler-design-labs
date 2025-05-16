
package edu.kit.kastel.vads.compiler.backend.asm;

import java.util.HashMap;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;



class X86RegisterSet {

    // private HashMap<Node, String> registers;
    private final HashMap<X86Register, Boolean> registersUsed;

    public X86RegisterSet() {
        // Construct the register set with the registers we can use for our computation
        this.registersUsed = new HashMap<>();
        this.registersUsed.put(new X86Register("r8"), false);
        this.registersUsed.put(new X86Register("r9"), false);
        this.registersUsed.put(new X86Register("r10"), false);
        this.registersUsed.put(new X86Register("r11"), false);
        this.registersUsed.put(new X86Register("r12"), false);
        this.registersUsed.put(new X86Register("r13"), false);
        this.registersUsed.put(new X86Register("r14"), false);
        this.registersUsed.put(new X86Register("r15"), false);
    }

    public X86Register reserveRegister() {
        for (var key : registersUsed.keySet()) {
            if (!registersUsed.get(key)) {
                registersUsed.put(key, true);
                return key;
            }
        }

        System.err.println("No unused register left! (This should not happen for SSA IR in Lab 1).");
        throw new RuntimeException("No unused register left!");
    }

    public void freeRegister(Register register) {
        assert(register instanceof X86Register);
        assert(registersUsed.containsKey((X86Register) register));
        registersUsed.put((X86Register) register, false);
    }


}