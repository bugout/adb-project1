default: RelevanceFeedback.class

RelevanceFeedback.class: RelevanceFeedback.java analyzer/*.java indexer/*.java util/*.java query/*.java searcher/*.java
	javac -cp ".:lib/*" RelevanceFeedback.java

# make run ARG1=api-key ARG2=precision ARG3='query'
# Note: please quote the query keywords properly
run: RelevanceFeedback.class
	java -cp ".:lib/*" RelevanceFeedback "${ARG1}" "${ARG2}" "$(ARG3)"

# Or you can run
# ./run.sh <api-key> <precision> <'query keywords'> 

# Or you can directly run the java command
#java -cp ".:lib/*" RelevanceFeedback <api-key> <precision> <'query keywords'>
