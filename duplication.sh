#!/bin/sh

trap "echo Interrupted; exit;" SIGINT SIGTERM

bin="java -jar duplication/out/artifacts/duplication_jar/duplication.jar"
dataset_dir="secondstring_set/arff"
unset _JAVA_OPTIONS

#for dataset in $(ls "$dataset_dir")
for dataset in "ucdPeopleCluster.arff"
do
	for entity_metric in {levenshtein,jaro-winkler,monge-elkan,soundex,cosine,jaccard}
	do
		threshold=0.9
		cmd="$bin  $dataset_dir/$dataset $entity_metric strict $threshold"
		echo "#$cmd"
		$cmd
	done
done

