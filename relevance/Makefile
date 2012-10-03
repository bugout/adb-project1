default: RelevanceFeedback.class

RelevanceFeedback.class: RelevanceFeedback.java analyzer/*.java indexer/*.java util/*.java query/*.java searcher/*.java
	javac -cp ".:lib/*" RelevanceFeedback.java

# To run our program, run
# ./run.sh <api-key> <precision> <'query keywords'> 

# Note: please quote the query keywords
