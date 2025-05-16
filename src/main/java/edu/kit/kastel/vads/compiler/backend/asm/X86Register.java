package edu.kit.kastel.vads.compiler.backend.asm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

class X86Register implements Register {



    public X86Register(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof  X86Register) && ((X86Register) o).name.equals(this.name);
    }

    @Override 
    public int hashCode() {
        return name.hashCode();
    }

    private final String name;
    
}