package ma.glasnost.orika.metadata;

public enum MappingDirection {
    
    BIDIRECTIONAL {
        @Override
        MappingDirection flip() {
            return BIDIRECTIONAL;
        }
    },
    
    A_TO_B {
        @Override
        MappingDirection flip() {
            return B_TO_A;
        }
    },
    
    B_TO_A {
        @Override
        MappingDirection flip() {
            return A_TO_B;
        }
    };
    
    abstract MappingDirection flip();
    
}
