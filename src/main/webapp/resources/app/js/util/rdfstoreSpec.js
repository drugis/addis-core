define(['rdfstore'], function(rdfstore) {

  describe('the rdf store', function() {
    it('should correctly query a typed literal', function() {
      expect(rdfstore).not.toBeUndefined();

      rdfstore.create(function(store) {
        store.load('text/turtle', '@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. <a> <b> "P4M"^^xsd:duration.',
          function(success, results) {
            if (!success) throw results;

            store.execute('SELECT * WHERE { ?s ?p ?o }',
              function(success, results) {
                if (!success) throw results;

                console.log(results);

              });
          });
      });

    });
  });

});
