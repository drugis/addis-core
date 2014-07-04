# addis-rdf

Convert ADDIS 1.x datafiles (.addis files) to RDF.

## Installation

You need Leiningen. You probably also want to make sure you have Apache Jena installed and have the command line tools on your path (try `arq --help`).

## Usage

Use `lein run`. See `importHypertension.sh` and `importDepression.sh` for examples.

## Options

```
 Switches               Default            Desc                
 --------               -------            ----                
 -h, --no-help, --help  false              Show Help           
 -f, --file                                ADDIS 1.x file      
 -n, --name                                Dataset short name  
 -t, --title            ADDIS data import  Dataset description 
 -r, --rdf              out.trig           RDF (TriG) file    
```

## Examples

Generate the example datasets:

```
./importDepression.sh; ./importHypertension.sh
```

Find drugs / outcomes / studies defined in the dataset (both datasets):

```
arq --data=depression.trig --query=dataset-drugs.sparql
arq --data=depression.trig --query=dataset-outcomes.sparql
arq --data=depression.trig --query=dataset-studies.sparql
```

Find instances of the drug Azilsartan (hypertension example):

```
arq --data=hypertension.trig --query=drug-instances.sparql
```

Find instances of the outcome "SBP mean trough (clinic, sitting)" (hypertension example):

```
arq --data=hypertension.trig --query=outcome-instances.sparql
```

Find arms where (only) Azilsartan was administered (hypertension example):

```
arq --data=hypertension.trig --query=arms-matching-drug.sparql
```

Find arms where Azilsartan + Chlortalidone was administered (hypertension example):

```
arq --data=hypertension.trig --query=arms-matching-combination.sparql
```

Find measurements of "HAM-D Responders" outcome in Fluoxetine arms (depression example):

```
arq --data=depression.trig --query=measurements.sparql
```

Generate an RDF/XML representation of the TriG (without named graphs):

```
trig depression.trig | sed 's/ <[^>]*> .$/ ./' | rdfcat -t - >depression.rdf
```

## Bugs

Many things are not yet imported; many more are untested; unit tests are non-existent.

## License

Copyright (c) 2014 Gert van Valkenhoef. GNU GPL Version 3 or later.
