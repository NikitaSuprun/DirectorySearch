# DirectorySearch

## Compile & Run

To compile the command line tool put the following commands into the terminal:

```
cd #PATH TO THIS FOLDER#
cd src/main/java
javac DirectorySearch.java
```

Now you should be able to run the tool. To launch the tool, put the following commands into the terminal:

```
java DirectorySearch #FULL PATH TO FOLDER#
```

If the path is invalid or does not contain any .txt files or files without extension, you will get an error!

## Usage

Input a query to search amongst all indexed filed and retrieve the top 10 results.

To exit the programme, input:

```
:quit
```

The search is case insensitive and ignores non-alphabetical characters.

The score is based on the total length of the words that were matched for each file.