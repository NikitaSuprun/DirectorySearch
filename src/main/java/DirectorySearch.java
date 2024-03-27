import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class DirectorySearch {
    private static final int OUTPUT_SIMILARITY_LIMIT = 10;
    private static final int SIMILARITY_SCORE_INITIAL_VALUE = -1;

    public static void main(String[] args) {

        /* We expect exactly one directory to be given as argument. */
        if (args.length == 0) {
            throw new IllegalArgumentException("No directory given to index.");
        }

        if (args.length > 1) {
            throw new IllegalArgumentException(
                    String.format("Too many arguments were given. Expected: 1, received: %d.", args.length)
            );
        }

        final File indexableDirectory = new File(args[0]);

        /* Check the given argument is a valid directory that exists. */
        if (!indexableDirectory.exists()) {
            throw new IllegalArgumentException(
                    String.format("Directory: %s does not exist.", indexableDirectory.getAbsolutePath())
            );
        }

        if (!indexableDirectory.isDirectory()) {
            throw new IllegalArgumentException(
                    String.format("%s is not a directory.", indexableDirectory.getAbsolutePath())
            );
        }

        List<IndexedFile> indexedFiles;

        try {
            indexedFiles = indexFiles(indexableDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (indexedFiles.size() == 0) {
            throw new IllegalArgumentException(
                    String.format("Directory: %s does not contain any text files (extension txt or none)!",
                            indexableDirectory.getAbsolutePath()));
        } else {
            System.out.println(String.format("%d files read in directory %s", indexedFiles.size(),
                    indexableDirectory.getAbsolutePath()));
        }

        /* Process input from the command line and execute search among indexed files. */
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            System.out.print("search> ");
            final String line = keyboard.nextLine();

            if (line.equals("")) {
            } else if (line.contains(":quit")) {
                return;
            } else { //If the query is not empty or exit, we assume it is a search query.

                /* Reassign similarity indexes, reorder list of indexed files and output top 10 similarity scores. */
                orderIndexedFiles(indexedFiles, line);
                outputSimilarityScores(indexedFiles);
            }
        }
    }

    protected static List<IndexedFile> indexFiles(File directory) throws IOException {
        List<IndexedFile> indexedFiles = new ArrayList<>(); //ArrayList is a suitable data structure for the use case.

        File[] indexableFiles = directory.listFiles();

        /* Create IndexedFile representation for each file and store all converted files in a list. */
        for (File file : indexableFiles) {
            if (file.isFile() && isTextFile(file)) {
                String fileName = getNameWithoutExtension(file);

                /* Throws IOException, up to the caller to handle. */
                String fileContentAsString = Files.readString(file.toPath());

                IndexedFile indexedFile = new IndexedFile(fileName, fileContentAsString);
                indexedFiles.add(indexedFile);
            }
        }

        return indexedFiles;
    }

    protected static void outputSimilarityScores(List<IndexedFile> indexedFiles) {
        int id = 0;
        while (id < indexedFiles.size() && id < OUTPUT_SIMILARITY_LIMIT) //Output only top 10 files.
        {
            IndexedFile indexedFile = indexedFiles.get(id); //indexedFiles is sorted.

            if (indexedFile.getSimilarityScore() == SIMILARITY_SCORE_INITIAL_VALUE) //Should never happen.
            {
                throw new RuntimeException(
                        String.format("The score for file %s was never initialised!", indexedFile.fileName));
            }

            /* If the top item in the list has similarity score 0, there are no matches. */
            if (indexedFile.getSimilarityScore() == 0 && id == 0) {
                System.out.println("no matches found");
                return;
            }

            System.out.println(
                    String.format("%s : %d %%", indexedFile.getFileName(), indexedFile.getSimilarityScore()));
            id += 1;
        }
    }

    protected static void orderIndexedFiles(List<IndexedFile> indexedFiles, String query) {
        for (IndexedFile file : indexedFiles) {
            file.assignSimilarityScore(query);
        }

        Collections.sort(
                indexedFiles, (f1, f2) -> f2.getSimilarityScore() - f1.getSimilarityScore()); //Descending order.
    }

    private static boolean isTextFile(File file) {
        String extension = getFileExtension(file);
        return extension.equals(".txt") || extension.equals(""); // We assume no extension implies a text file.
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        return (lastIndexOf == -1) ? "" : name.substring(lastIndexOf); //Extension is optional.
    }

    private static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex == -1) ? name : name.substring(0, dotIndex); //Extension is optional.
    }

    protected static class IndexedFile {
        private final String fileName;
        private final Map<String, Integer> fileContent;
        private int similarityScore;

        protected IndexedFile(String fileName, String fileContent) {
            this.fileName = fileName;
            this.fileContent = convertContentStringToMap(fileContent);
            this.similarityScore = SIMILARITY_SCORE_INITIAL_VALUE; //Initial dummy value.
        }

        protected void assignSimilarityScore(String query) {
            /* Only letter-characters constitute the length of the query. */
            int queryLength = query.replaceAll("[^a-zA-Z]", "").length();
            int matchedLength = 0;

            Map<String, Integer> queryMap = convertContentStringToMap(query);

            for (String queryWord : queryMap.keySet()) // Count length of the matched words.
            {
                if (fileContent.containsKey(queryWord)) {
                    int queryWordCount = queryMap.get(queryWord);
                    int fileWordCount = fileContent.get(queryWord);

                    /* Case: File contains the same or higher count of the word. Match the full count of the word. */
                    if (fileWordCount >= queryWordCount) {
                        matchedLength += queryWordCount * queryWord.length();
                    }
                    /* Case: Query contains a higher count of the word than what is present in the file.
                    Only match the count of the word present in the file. */
                    else {
                        matchedLength += fileWordCount * queryWord.length();
                    }
                }
            }

            /* Convert proportion to percentage. */
            similarityScore = Math.round(((float) matchedLength / queryLength) * 100);
        }

        private Map<String, Integer> convertContentStringToMap(String content) {
            content = content.replaceAll("[^a-zA-Z]", " "); //Disregard all non-letter characters.
            content = content.toLowerCase(); //Make words case-insensitive.

            String[] words = content.split(" ");
            Map<String, Integer> wordMap = new HashMap<>();

            for (String word : words) //Populate wordMap.
            {
                if (word.equals("") || word.equals(" "))
                    continue;

                if (!wordMap.containsKey(word)) {
                    wordMap.put(word, 0);
                }

                wordMap.put(word, wordMap.get(word) + 1);
            }

            return wordMap;
        }

        protected int getSimilarityScore() {
            return similarityScore;
        }

        protected String getFileName() {
            return fileName;
        }

        protected Map<String, Integer> getFileContent() {
            return fileContent;
        }
    }
}