ADDIS 2: Initial ConceptMapper setup
====================================

The [ADDIS 2 architecture][architecture] calls for a concept mapper component
that enables post-hoc harmonization of datasets by exploiting both existing
terminologies and ontologies and annotations provided by users.

The [semantic web][semanticweb] and linked data research community have
provided a rich body of research, standards, and technologies enabling the
computer to reason about linked data supported by ontologies of varying
complexity. Thus, we sought to build a prototype of concept mapper using
these technologies.

A quick overview of the Semantic Web
------------------------------------

FIXME: what is the semantic web

### Resource Description Framework (RDF) ###

FIXME: what is RDF; serialization formats, Turtle.

### Named Graphs ###

### Querying RDF: SPARQL ###

FIXME: SPARQL, including REST API.

### Implicit knowledge ###

FIXME: property paths and reasoning

### Triple stores ###

FIXME: what are triple stores, varying features

Setting up the triple store
---------------------------

After reading up on various triple stores, we set up 4store, OpenRDF Sesame,
and Apache Jena + Fuseki for further evaluation.  It turned out that 4store
did not support SPARQL property paths. Reasoning support for 4store is
limited, with only a subset of RDFS reasoning available in a fork of the
project. This support does appear to have been merged into the main source
repository, however its status is unclear and documentation is lacking.
Sesame did appear to have the right feature set, but we had trouble loading
the largest terminologies. Apache Jena with TDB storage and the Fuseki web
frontend offered acceptable loading performance and flexibibility in enabling
various services (SPARQL update, reasoning, etc.). The W3C comparison of
[large triple stores][large] also claims that Jena TDB can handle much larger
sets of triples than Sesame, which had been tested only up to 70M triples,
a limit easily exceeded by our use cases.

Jena is installed under its own user on our workstations rather than
in a virtual machine, to maximize the memory available (our machines have
only 4GB memory - for now). The following lines in the `.profile` enable the
Jena and Fuseki command line tools (**NB: update versions to the ones you download**):

    export JENA_HOME=$HOME/apache-jena-2.11.0
    export FUSEKI_HOME=$HOME/jena-fuseki-1.0.1-SNAPSHOT
    export PATH=$FUSEKI_HOME:$JENA_HOME/bin:$PATH

The most important tool from the Jena distribution is `tdbloader`, which
enables us to load large RDF files to named graphs in bulk.

    tdbloader --loc DB --graph=URI file.rdf

Replace 'URI' with the URI of the graph to load the data into, and 'file.rdf' with the file you want to load. Note that .ttl files can also be loaded. This will create the `DB` directory and initialize a new triple store if one doesn't exist. Otherwise, the existing triple store at that location will be updated. 

A SPARQL-update and SPARQL-HTTP enabled endpoint can be run using Fuseki as follows:

    fuseki-server --loc DB --update /ds

This will start a fuseki service with update capability on
`http://localhost:3030/ds/data`.

Such a Fuseki instance easily serves the terminologies we're interested in.
However, many of our use cases require some form of text matching. This is
supported in SPARQL using the `regex()` function. However, on our full triple
store, queries involving `regex()` can often take many minutes. Therefore, we
need an index to improve performance of text matching queries. To enable text indexing, a custom Jena assembler definition [desc.ttl](desc.ttl) is
needed.

This loads the TDB storage mechanism as well as the text indexing
capabilities. Then, it defines a Fuseki service that exposes a text-indexed
TDB dataset in both read-only and read/write modes. The TDB dataset is still
stored under `DB`, while the text index is stored under `DBLucene`. Now, the
initial text index is created with:

    java -cp $FUSEKI_HOME/fuseki-server.jar jena.textindexer --desc=desc.ttl

And the Fuseki server is started as follows:

    fuseki-server --conf=desc.ttl

This enables a special `text:query` predicate that allows us to perform text
searches using the Lucene query language within SPARQL, e.g.:

    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    PREFIX snomed: <http://www.ihtsdo.org/>
    PREFIX text:    <http://jena.apache.org/text#> 

    SELECT * WHERE {
      GRAPH <http://www.ihtsdo.org/SNOMEDCT/> {
        ?uri rdfs:subClassOf snomed:SCT_64572001 .
        ?uri text:query (rdfs:label 'Fistula*') .
        ?uri rdfs:label ?label .
      }
    }

The text index currently has some shortcomings:

 1. It seems to only index nodes of the type `xsd:string`. However, some of the terminologies we import specify the `xml:lang`, which (in turtle format at least) precludes also specifying `xsd:string`. It is unclear whether we should recode these terminologies to replace `xml:lang` specifications with the `xsd:string` type or whether the indexer can be told to also index nodes with an `xml:lang`.

 2. Lucene does not allow prefix wildcards (e.g. *tension), but it may often be desirable to discard some prefixes (e.g. 'anti' in 'antidepressants').

We're looking at how to optimally configure the text indexing.

Importing terminologies
-----------------------

### SNOMED CT

SNOMED CT is used in ADDIS 1.x to code indications (i.e. medical conditions
or diseases). To exploit this meta-data already present in ADDIS files, we
need to load the SNOMED CT terminology into ConceptMapper. Unlike many other
medical terminologies, SNOMED CT is developed using description logic tools.
While SNOMED CT does have its own release format, the standard release comes
with a Perl script that transforms this format into RDF. To load SNOMED CT,
we performed the following steps:

 1. Download `SnomedCT_Release_INT_20130131` from the UMLS service.

 2. Convert the stated relationships to RDF using the supplied Perl script.

 3. Use [Protege][protege] and the [Hermit][hermit] reasoner to classify SNOMED CT and export the inferred relationships.

 4. Load both sets into a graph named `http://www.ihtsdo.org/SNOMEDCT/`. Example commands:

        tdbloader --loc DB --graph=http://www.ihtsdo.org/SNOMEDCT/ snomed.rdf
        tdbloader --loc DB --graph=http://www.ihtsdo.org/SNOMEDCT/ snomed-inferred.ttl

### ATC classification

The ATC classification is not a true ontology, but it does provide a rather
comprehensive listing of drugs, especially those licensed for use in Europe.
ATC is used in ADDIS 1.x to code drugs, so it is also important to be able to
import it. We acquired the ATC 2011 release in Excel format and exported it
to CSV. Then, we wrote a small Python script to transform the CSV data to
Turtle format. It is not entirely clear how the ATC hierarchy can best be
coded as RDF, but for now we've settled upon using rdfs:subClassOf within the
hierarchy, introducing `atc:ATCCode` as the top-level term. We loaded the resulting triples into the `http://www.whocc.no/ATC2011/` graph.

### ICD-10, MedDRA, LOINC

We used the UMLS distribution (mmsys) and NCBO's [umls2rdf][umls2rdf] scripts to import the ICD-10, MedDRA, and LOINC ontologies. This entails the following:

 1. Download the UMLS release files (2013AA)

 2. Extract the UMLS release files

 3. Run mmsys to 'install' the release files, and generate a MySQL import script

 4. Import the generated MySQL files into a MySQL database

 5. Configure and run the `umls2rdf` scripts
 
FIXME: some questions raised about output ontology, availability of more canonical (i.e. non-UMLS-distorted) versions?

Importing ADDIS datasets
------------------------

The `org.drugis.importer` Clojure project allows the importing of ADDIS datasets into a PostgreSQL database, and exporting of metadata to a `.ttl` file. The importer checks for each item whether there is a known code system to which it is mapped, for example ATC for drugs. If there is, a mapping between the item and the encoded concept is added. 

Example command line for importing a specific dataset (assumes [leiningen][leiningen] is installed and an [initialised trialverse PostgreSQL database](../dbms) is set up):

    lein run --database "postgresql://localhost?user=<user>&password=<passwd>" \\
             --rdf "depression.ttl" \\
             --file "example.addis" \\
             --name "example dataset" \\
             --title "example title"

By convention each ADDIS dataset should be loaded into its own graph labeled `http://trials.drugis.org/namespaces/<number>/`, where '<number>' is the id given by the importer.

The importer will output the namespace metadata necessary to access the imported data. These data should be saved to a .ttl file and loaded into the `http://trials.drugis.org/namespaces/` graph via `tdbloader`.

Querying
--------

### Finding concepts ###

### Finding instances ###

Web Frontend
------------

Once an instance of fuseki is running with loaded data, it can be accessed via our [SPARQL frontend](../sparql). To run this, navigate to the sparql directory and execute `python -m SimpleHTTPServer 3000`. Now the frontend is available at [http://localhost:3000/].

Conclusion
----------

[architecture]: http://drugis.org/files/20130319-addis2-architecture.pdf
    "ADDIS 2.x Requirements and Architecture"
[semanticweb]: http://www.w3.org/2001/sw/
    "W3C Semantic Web Activity Homepage"
[large]: http://www.w3.org/wiki/LargeTripleStores
    "Large Triple Stores"
[jena-text]: http://jena.apache.org/documentation/query/text-query.html
    "Text searches with SPARQL"
[protege]: http://protege.stanford.edu/
    "Ontology editor and knowledge-base framework"
[hermit]: http://hermit-reasoner.com/
    "Reasoner for ontologies"
[umls2rdf]: https://github.com/ncbo/umls2rdf
    "Take the MYSQL Unified Medical Language System (UMLS) database to convert the ontologies to RDF using OWL and SKOS as main schemas."
[leiningen]: https://github.com/technomancy/leiningen
    "Tool for running Clojure programs"