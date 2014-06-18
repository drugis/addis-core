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

```
./importDepression.sh
arq --data=depression.trig --query=query.sparql
```

### Bugs

Many things are not yet imported; many more are untested; unit tests are non-existent.

## License

Copyright (c) 2014 Gert van Valkenhoef. GNU GPL Version 3 or later.
