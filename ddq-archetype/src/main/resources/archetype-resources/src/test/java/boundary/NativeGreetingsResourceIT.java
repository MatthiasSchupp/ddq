package ${package}.boundary;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeGreetingsResourceIT extends GreetingsResourceTest {

    // Execute the same tests but in native mode.
}