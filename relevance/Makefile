default: RelevanceFeedback.class

RelevanceFeedback.class: RelevanceFeedback.java analyzer/*.java indexer/*.java util/*.java query/*.java searcher/*.java
	javac -cp ".:lib/*" RelevanceFeedback.java

run:
	java -cp ".:lib/*" RelevanceFeedback "$(ARG1)" "$(ARG2)" "$(ARG3)"