package OOP.Tests.Example;
import OOP.Solution.OOPMethod;
import OOP.Solution.OOPModifier;
import OOP.Provided.OOPMultipleException;
import OOP.*;

public interface I1 {

    @OOPMethod(modifier = OOPModifier.PUBLIC)
    String f() throws OOPMultipleException;
}
