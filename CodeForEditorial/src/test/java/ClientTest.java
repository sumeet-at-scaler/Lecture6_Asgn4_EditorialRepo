import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testClientClassExists() {
        try {
            Class.forName("Client");
        } catch (ClassNotFoundException e) {
            fail("Client class not found");
        }
    }

    @Test
    public void testArrayCreatorClassExists() {
        try {
            Class.forName("ArrayCreator");
        } catch (ClassNotFoundException e) {
            fail("ArrayCreator class not found");
        }
    }

    @Test
    public void testClientMainMethod() throws ClassNotFoundException {
        Class<?> clientClass = Class.forName("Client");
        boolean hasMainMethod = false;
        for (Method method : clientClass.getDeclaredMethods()) {
            if (method.getName().equals("main")) {
                hasMainMethod = true;
                break;
            }
        }
        assertTrue(hasMainMethod);
    }

    @Test
    public void testArrayCreatorCallableInterface() throws Exception {
        assertTrue(Callable.class.isAssignableFrom(Class.forName("ArrayCreator")));
    }

    @Test
    public void testArrayCreatorConstructor() throws Exception {
        Constructor<?> acConstructor = Class.forName("ArrayCreator").
                                                getDeclaredConstructor(int.class);
        assertNotNull(acConstructor);
    }

    @Test
    public void testArrayCreatorOutput() {
        try {
            Constructor<?> acConstructor = Class.forName("ArrayCreator").
                    getDeclaredConstructor(int.class);
            Object ac = acConstructor.newInstance(5);
            Method callMethod = ac.getClass().getDeclaredMethod("call");
            Object result = callMethod.invoke(ac);
            if (result instanceof ArrayList<?> &&
                    ((ArrayList<?>) result).get(0) instanceof Integer) {
                ArrayList<Integer> arrayListResult = (ArrayList<Integer>) result;
                if(arrayListResult.size() == 5){
                    if(!arrayListResult.toString().equals("[1, 2, 3, 4, 5]")){
                        fail("The returned arraylist should contain numbers from 1 to n");
                    }
                } else {
                    fail("ArrayCreator is not adding sufficient elements to returned arraylist.");
                }
            } else {
                fail("Implement Callable<ArrayList<Integer>>");
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testArrayCreatorFromMain() throws InterruptedException {
        try {
            String input = "5\n";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputStream);

            Method mainMethod = Class.forName("Client").getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, (Object)new String[0]);

            assertEquals("[1, 2, 3, 4, 5]\n", outContent.toString(), "The returned arraylist should contain numbers from 1 to n");
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}