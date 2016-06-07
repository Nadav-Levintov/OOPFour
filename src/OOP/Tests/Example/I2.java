package OOP.Tests.Example;

import OOP.Provided.OOPMultipleException;
import OOP.Solution.OOPMethod;
import OOP.Solution.OOPModifier;
public interface I2 {

    @OOPMethod(modifier = OOPModifier.PUBLIC)
    void g() throws OOPMultipleException;
}
