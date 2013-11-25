import csv

print "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
print "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
print "@prefix atc: <http://www.whocc.no/ATC2011/> ."
print "@prefix owl:  <http://www.w3.org/2002/07/owl#> ."

print """<http://www.whocc.no/ATC2011/> a owl:Ontology ;
    rdfs:comment "RDF Version of the ATC 2011" ;
    rdfs:label "ATC" ;
    owl:versionInfo "2011" ."""

print """atc:ATCCode a owl:Class ;
	rdfs:label "ATC Code" ."""

with open("ATC2011.csv", "r") as csvfile:
	reader = csv.DictReader(csvfile)
	for line in reader:
		code = line['ATCCode']
		if len(code) == 1: # Level-1 code
			line['super'] = "ATCCode"
		elif len(code) == 3: # Level-2 code
			line['super'] = code[0]
		elif len(code) == 4: # Level-3 code
			line['super'] = code[:3]
		elif len(code) == 5: # Level-4 code
			line['super'] = code[:4]
		elif len(code) == 7: # Level-5 code
			line['super'] = code[:5]
		else:
			raise "ERRORRRR"
		print "atc:%(ATCCode)s a owl:Class ;\n  rdfs:label \"%(Name)s\" ;\n  rdfs:subClassOf atc:%(super)s ." % line
