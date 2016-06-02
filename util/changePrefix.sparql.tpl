PREFIX void: <http://rdfs.org/ns/void#>
PREFIX es: <http://drugis.org/eventSourcing/es#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

DELETE {
  ?old ?p ?o .
} INSERT {
  ?new ?p ?o .
} WHERE {
  BIND("OLDPREFIX"^^xsd:string AS ?oldPrefix)
  BIND("NEWPREFIX"^^xsd:string AS ?newPrefix)
  BIND(STRLEN(?oldPrefix) AS ?len)

  ?old ?p ?o .
  BIND (IRI(CONCAT(?newPrefix, SUBSTR(str(?old), ?len + 1))) AS ?new)
  FILTER (?oldPrefix=SUBSTR(str(?old), 1, ?len))
};

DELETE {
  ?s ?p ?old .
} INSERT {
  ?s ?p ?new .
} WHERE {
  BIND("OLDPREFIX"^^xsd:string AS ?oldPrefix)
  BIND("NEWPREFIX"^^xsd:string AS ?newPrefix)
  BIND(STRLEN(?oldPrefix) AS ?len)

  ?s ?p ?old .
  BIND (IRI(CONCAT(?newPrefix, SUBSTR(str(?old), ?len + 1))) AS ?new)
  FILTER (?oldPrefix=SUBSTR(str(?old), 1, ?len))
};

DELETE {
  GRAPH ?old {
    ?s ?p ?o .
  }
} INSERT {
  GRAPH ?new {
    ?s ?p ?o .
  }
} WHERE {
  BIND("OLDPREFIX"^^xsd:string AS ?oldPrefix)
  BIND("NEWPREFIX"^^xsd:string AS ?newPrefix)
  BIND(STRLEN(?oldPrefix) AS ?len)

  GRAPH ?old {
    ?s ?p ?o
  }

  BIND (IRI(CONCAT(?newPrefix, SUBSTR(str(?old), ?len + 1))) AS ?new)
  FILTER (?oldPrefix=SUBSTR(str(?old), 1, ?len))
}
