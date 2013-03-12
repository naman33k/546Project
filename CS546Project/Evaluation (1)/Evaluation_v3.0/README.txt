
1. Setup
java -cp .:lib/edison-0.2.5.jar:lib/coreUtilities-0.1.1.jar:lib/libthrift-0.4.0.jar:lib/curator-interfaces-0.6.jar:lib/LBJ2Library-2.7.2.jar:lib/slf4j-api-1.6.1.jar:lib/commons-lang-2.5.jar:bin:/home/wieting2/Reader/bin/ evaluation.Main1

This evaluation program currently allows for evaluation of tasks 1 and 2 (Task 3 evaluation will be up soon). The program can be run as library or from the command line. For each task, an example main function is implemented, Main1 and Main2. Each of these generates an output file for each task and evaluates if for its statistics. To compile use (Note that you need to provide the path to the Reader bin - dont use mine):

javac -d bin -cp .:lib/edison-0.2.5.jar:lib/coreUtilities-0.1.1.jar:lib/libthrift-0.4.0.jar:lib/curator-interfaces-0.6.jar:lib/LBJ2Library-2.7.2.jar:/home/wieting2/Reader/bin/ src/evaluation/*.java

2. Main
In general there is a Main class and a TaskEval class for each task. For the Main class: 

java -cp .:lib/edison-0.2.5.jar:lib/coreUtilities-0.1.1.jar:lib/libthrift-0.4.0.jar:lib/curator-interfaces-0.6.jar:lib/LBJ2Library-2.7.2.jar:lib/slf4j-api-1.6.1.jar:lib/commons-lang-2.5.jar:bin:/home/wieting2/Reader/bin/ evaluation.Main[1 or 2] <path to filelist> <path>

where <path to filelist> refers to a list of ace2004 files (complete file names not just the stems - the code looks for .sgm to figure out the stem). <path> is optional and is only needed if an additional path is required to read in the files from the filelist (for instance I need to include "home/wieting2/" as my filelist doesn't include this prefix).

3. TaskEval
To treat as a library just use the TaskEval constructors, specifiying the file that contains your results. If a path must be specified to read in the ace data include this as a second argument.

To run from the commandline use the command use:

java -cp .:lib/edison-0.2.5.jar:lib/coreUtilities-0.1.1.jar:lib/libthrift-0.4.0.jar:lib/curator-interfaces-0.6.jar:lib/LBJ2Library-2.7.2.jar:lib/slf4j-api-1.6.1.jar:lib/commons-lang-2.5.jar:bin:/home/wieting2/Reader/bin/ evaluation.Task[1 or 2]Eval <path to output file> <path>

4. Output file format
The output file for tasks 1 and 2 are different. Both files are csv (comma separated - see output file example included). An Example for task2 is included (output.txt).  For task 1 for file should be:

<ace file stem>, <relationLabel>, <mentionID1>, <mentionID2>, <sentenceID>

<ace file stem>: path to ace file where prediction was made (if that is not enough info to find file before to include the path as a separate argument)

<relationLabel>: The label as used in the SemanticRelation class (make sure to use the fine label)
<mentionID1>: The mention ID of the relation's first mention as mentioned in the Mention class
<mentionID2>: The mention ID of the relation's second mention as mentioned in the Mention class
<sentenceID>: The sentenceID where the predicted relation had been.

IMPORTANT: ADD "Null" to the label of relations you predict to be null. We are tracking this relation as well. Also for a given sentence you must predict all possible relations - which is 2*(N \choose 2) where N is the number of mentions.

For task 2, the above method is used for the relations. Additionally the following format is used for the mentions

<ace file stem>, <mentionLabel>, <mentionID>, <sentenceID>

The only thing that needs mentioning here is that the <mentionLabel> is the coarse label (SC in the Mention class). This will be a three letter word.

The program calculates the accuracy, precision, recall, and F1 for each type of relation and mention. If a NaN appears that just means that not enough examples of that type were seen to calculate one of the properties. Thus this type is not used in calculating the average F1.

Please do not hesitate to email wieting2@illinois.edu if there are any issues or difficulties. Thanks.
