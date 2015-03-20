// An alternative example could be..
define('testUtils', ['lodash'],
  function(_) {
    var testFusekiUri = 'http://localhost:9876/scratch';

    function queryTeststore(query) {
      //console.log('queryTeststore query = ' + query);
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open('POST', testFusekiUri + '/query?output=json', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-query');
      xmlHTTP.setRequestHeader('Accept', 'application/ld+json');
      xmlHTTP.send(query);
      var result = xmlHTTP.responseText;
      // console.log('queryTeststore result = ' + result);
      return result;
    }

    function dropGraph(uri) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open('POST', testFusekiUri + '/update', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
      xmlHTTP.send('DROP GRAPH <' + uri + '>');
      return true;
    }

    function loadTemplate(templateName, httpBackend) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open('GET', 'base/app/sparql/' + templateName, false);
      xmlHTTP.send(null);
      var template = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/' + templateName).respond(template);
      return template;
    }

    function executeUpdateQuery(query) {
      //console.log('executeUpdateQuery: ' + query);
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open('POST', testFusekiUri + '/update', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
      xmlHTTP.send(query);
      return xmlHTTP.responseText;
    }

    function deFusekify(data) {
      // console.log('deFusekify on : ' + data);
      var json = JSON.parse(data);
      var bindings = json.results.bindings;
      return _.map(bindings, function(binding) {
        return _.object(_.map(_.pairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    function createRemoteStoreStub() {
      return jasmine.createSpyObj('RemoteRdfStoreService', [
        'create',
        'load',
        'executeUpdate',
        'executeQuery',
        'getGraph',
        'deFusekify'
      ]);
    }

    return {
      queryTeststore: queryTeststore,
      dropGraph: dropGraph,
      loadTemplate: loadTemplate,
      executeUpdateQuery: executeUpdateQuery,
      deFusekify: deFusekify,
      createRemoteStoreStub: createRemoteStoreStub
    };
  }
);
