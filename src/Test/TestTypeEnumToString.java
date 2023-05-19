import org.junit.*;
import org.junit.runners.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.*;

import javax.sound.sampled.AudioFileFormat.Type;

@RunWith(Parameterized.class)
public class TestTypeEnumToString{
    TypeEnum type;
    String typeString;
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{TypeEnum.Int, "int "},{TypeEnum.Float, "double "},
        {TypeEnum.String, "char *"},{TypeEnum.NN, "model_T "}});
    }
    public TestTypeEnumToString(TypeEnum Type, String output){
        this.type = Type;
        this.typeString = output;
    }  
    @Test
    public void testTypeEnumToString(){
        assertEquals(typeString+"PLACEHOLDER", ToCCompiler.TypeEnumToString(type));
    }
}