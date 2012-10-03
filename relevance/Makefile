default: RelevanceFeedback.class

RelevanceFeedback.class: RelevanceFeedback.java analyzer/*.java indexer/*.java util/*.java query/*.java searcher/*.java
	javac -cp ".:lib/*" RelevanceFeedback.java

# To run our program simply run 
# java -cp ".:lib/*" RelevanceFeedback <api-key> <precision> <'query keywords'>
