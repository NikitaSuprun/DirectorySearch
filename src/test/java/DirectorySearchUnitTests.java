import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectorySearchUnitTests {

    @Test
    @DisplayName("Unit test indexFiles.")
    void testIndexFiles() throws IOException {
        final File testDirectory = new File("src/test/resources/indexFiles");

        List<DirectorySearch.IndexedFile> indexedFiles = DirectorySearch.indexFiles(testDirectory);

        DirectorySearch.IndexedFile firstFile = indexedFiles.get(0);
        assertTrue(firstFile.getFileName().equals("testIndexFiles1"));
        assertTrue(firstFile.getFileContent().containsKey("one"));
        assertEquals(2, firstFile.getFileContent().get("one"));
        assertEquals(1, firstFile.getFileContent().get("two"));

        DirectorySearch.IndexedFile secondFile = indexedFiles.get(1);
        assertTrue(secondFile.getFileName().equals("testIndexFiles2"));
        assertTrue(secondFile.getFileContent().containsKey("word"));
        assertFalse(secondFile.getFileContent().containsKey(" "));
        assertFalse(secondFile.getFileContent().containsKey(""));
        assertEquals(3, secondFile.getFileContent().get("word"));
    }

    @Test
    @DisplayName("Unit test assignSimilarityScore.")
    void testAssignSimilarityScore() {
        DirectorySearch.IndexedFile indexedFile = new DirectorySearch.IndexedFile("name", "one");
        indexedFile.assignSimilarityScore("one");
        assertEquals(100, indexedFile.getSimilarityScore());

        indexedFile.assignSimilarityScore("two");
        assertEquals(0, indexedFile.getSimilarityScore());

        indexedFile.assignSimilarityScore("one one one");
        assertEquals(33, indexedFile.getSimilarityScore());

        indexedFile.assignSimilarityScore("one two");
        assertEquals(50, indexedFile.getSimilarityScore());
    }

    @Test
    @DisplayName("Unit test assignSimilarityScore.")
    void testOrderIndexedFiles() {
        List<DirectorySearch.IndexedFile> indexedFiles = new ArrayList<>();
        DirectorySearch.IndexedFile indexedFileOne = new DirectorySearch.IndexedFile("1", "one");
        DirectorySearch.IndexedFile indexedFileTwo = new DirectorySearch.IndexedFile("2", "two");
        indexedFiles.add(indexedFileOne);
        indexedFiles.add(indexedFileTwo);

        DirectorySearch.orderIndexedFiles(indexedFiles, "one");
        assertEquals("1", indexedFiles.get(0).getFileName());
        assertEquals("2", indexedFiles.get(1).getFileName());
    }

    @Test
    @DisplayName("Unit test outputSimilarityScores.")
    void testOutputSimilarityScores() {
        final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        List<DirectorySearch.IndexedFile> indexedFiles = new ArrayList<>();

        DirectorySearch.IndexedFile indexedFileOne = new DirectorySearch.IndexedFile("1", "one");
        DirectorySearch.IndexedFile indexedFileTwo = new DirectorySearch.IndexedFile("2",
                "one one");
        DirectorySearch.IndexedFile indexedFileThree = new DirectorySearch.IndexedFile("3",
                "one one one");
        DirectorySearch.IndexedFile indexedFileFour = new DirectorySearch.IndexedFile("4",
                "one one one one");
        DirectorySearch.IndexedFile indexedFileFive = new DirectorySearch.IndexedFile("5",
                "one");
        DirectorySearch.IndexedFile indexedFileSix = new DirectorySearch.IndexedFile("6",
                "one");
        DirectorySearch.IndexedFile indexedFileSeven = new DirectorySearch.IndexedFile("7",
                "one");
        DirectorySearch.IndexedFile indexedFileEight = new DirectorySearch.IndexedFile("8",
                "one");
        DirectorySearch.IndexedFile indexedFileNine = new DirectorySearch.IndexedFile("9",
                "one");
        DirectorySearch.IndexedFile indexedFileTen = new DirectorySearch.IndexedFile("10",
                "one");
        DirectorySearch.IndexedFile indexedFileEleven = new DirectorySearch.IndexedFile("11",
                "two");

        indexedFiles.add(indexedFileOne);
        indexedFiles.add(indexedFileTwo);
        indexedFiles.add(indexedFileThree);
        indexedFiles.add(indexedFileFour);
        indexedFiles.add(indexedFileFive);
        indexedFiles.add(indexedFileSix);
        indexedFiles.add(indexedFileSeven);
        indexedFiles.add(indexedFileEight);
        indexedFiles.add(indexedFileNine);
        indexedFiles.add(indexedFileTen);
        indexedFiles.add(indexedFileEleven);

        DirectorySearch.orderIndexedFiles(indexedFiles, "one one one one");
        DirectorySearch.outputSimilarityScores(indexedFiles);
        Assertions.assertTrue(outputStreamCaptor.toString().contains("4 : 100 %"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("3 : 75 %"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("2 : 50 %"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("1 : 25 %"));
        Assertions.assertFalse(outputStreamCaptor.toString().contains("11"));
    }
}