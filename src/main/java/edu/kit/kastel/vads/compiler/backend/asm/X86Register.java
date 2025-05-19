package edu.kit.kastel.vads.compiler.backend.asm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

class X86Register implements Register {

    public X86Register(String name) {
        this.name = name;
        this.spilled = false;
        this.offset = -1;
    }
    
    public X86Register(String name, boolean spilled, int offset) {
        this.name = name;
        this.spilled = spilled;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof  X86Register) && ((X86Register) o).name.equals(this.name);
    }

    @Override 
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isSpilled() {
        return spilled;
    }

    public int getOffset() {
        return offset;
    }

    private final boolean spilled;
    private final String name;
    private final int offset;
    
}