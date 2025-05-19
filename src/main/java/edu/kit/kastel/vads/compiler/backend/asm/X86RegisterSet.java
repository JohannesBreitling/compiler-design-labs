
package edu.kit.kastel.vads.compiler.backend.asm;

import java.util.HashMap;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;



class X86RegisterSet {

    // private HashMap<Node, String> registers;
    private final HashMap<X86Register, Boolean> registersUsed;

    public X86RegisterSet() {
        // Construct the register set with the registers we can use for our computation
        this.registersUsed = new HashMap<>();
        this.registersUsed.put(new X86Register("r8d"), false);
        this.registersUsed.put(new X86Register("r9d"), false);
        this.registersUsed.put(new X86Register("r10d"), false);
        this.registersUsed.put(new X86Register("r11d"), false);
        this.registersUsed.put(new X86Register("r12d"), false);
        this.registersUsed.put(new X86Register("r13d"), false);
        this.registersUsed.put(new X86Register("r14d"), false);
        this.registersUsed.put(new X86Register("r15d"), false);
    }

    public X86Register reserveRegister() {
        for (var key : registersUsed.keySet()) {
            if (!registersUsed.get(key)) {
                registersUsed.put(key, true);
                // System.out.println("Reseverd: " + key.toString());
                return key;
            }
        }

        System.err.println("No unused register left! (This should not happen for SSA IR in Lab 1).");
        throw new RuntimeException("No unused register left!");
    }

    public void freeRegister(Register register) {
        X86Register converted = (X86Register) register;

        assert registersUsed.containsKey(converted);
        // System.out.println("Freed: " + converted.toString());
        registersUsed.put(converted, false);
    }


}