import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class DirectorySearchMainTests {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor)); //Used to capture print statements.
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("No directory given.")
    void testNoDirectoryGivenThrowsIllegalArgumentException() {
        String[] emptyArguments = new String[0];

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> DirectorySearch.main(emptyArguments));
        assertEquals("No directory given to index.", exception.getMessage());
    }

    @Test
    @DisplayName("Too many arguments given.")
    void testTooManyArgumentsThrowsIllegalArgumentException() {
        String[] arguments = {"/path1", "/path2"};

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> DirectorySearch.main(arguments));
        assertEquals("Too many arguments were given. Expected: 1, received: 2.", exception.getMessage());
    }

    @Test
    @DisplayName("Argument is not a directory.")
    void testArgumentIsNotADirectoryThrowsIllegalArgumentException() {
        String[] arguments = {"src/test/resources/test0/filename1.txt"};

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> DirectorySearch.main(arguments));
        assertTrue(exception.getMessage().contains("src/test/resources/test0/filename1.txt is not a directory."));
    }

    @Test
    @DisplayName("Non-existent directory.")
    void testNonExistentDirectoryThrowsIllegalArgumentException() {
        String[] arguments = {"src/test/resources/test0/nonexistent"};

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> DirectorySearch.main(arguments));
        assertTrue(exception.getMessage().contains("src/test/resources/test0/nonexistent does not exist."));
    }

    @Test
    @DisplayName("Non-existent directory.")
    void testEmptyDirectoryThrowsIllegalArgumentException() {
        String[] arguments = {"src/test/resources/test0/empty"};

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> DirectorySearch.main(arguments));
        assertTrue(exception.getMessage()
                .contains("src/test/resources/test0/empty does not contain any text files (extension txt or none)!"));
    }

    @Test
    @DisplayName("Read in text files.")
    void testReadInThreeTextFiles() {
        String[] arguments = {"src/test/resources/test0"};
        provideInput(":quit");
        DirectorySearch.main(arguments);

        Assertions.assertTrue(outputStreamCaptor.toString().contains("3 files read in directory"));
    }

    @Test
    @DisplayName("One full match and one no match.")
    void testOneFullMatchAndOneNoMatch() {
        String[] arguments = {"src/test/resources/test1"};
        provideInput("WORD \n :quit");
        DirectorySearch.main(arguments);

        Assertions.assertTrue(outputStreamCaptor.toString().contains("full_match : 100 %"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("no_match : 0 %"));
    }

    @Test
    @DisplayName("No matches found.")
    void testOneFullMatchAndNoMatch() {
        String[] arguments = {"src/test/resources/test1"};
        provideInput("No matches found :) \n :quit");
        DirectorySearch.main(arguments);

        Assertions.assertTrue(outputStreamCaptor.toString().contains("no matches found"));
    }

    @Test
    @DisplayName("30% match and 10% match.")
    void testThirtyPercentMatchAndTenPercentMatch() {
        String[] arguments = {"src/test/resources/test2"};
        provideInput("a b c d e f g h j k \n :quit");
        DirectorySearch.main(arguments);

        Assertions.assertTrue(outputStreamCaptor.toString().contains("10_percent_match : 10 %"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("30_percent_match : 30 %"));
    }
}