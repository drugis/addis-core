'use strict';
  function foo () {
    var TestUtilService = function() {
      function queryTeststore(query, scratchStudyUri) {
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('POST', scratchStudyUri + '/query?output=json', false);
        xmlHTTP.setRequestHeader('Content-type', 'application/sparql-query');
        xmlHTTP.setRequestHeader('Accept', 'application/ld+json');
        xmlHTTP.send(query);
        var result = xmlHTTP.responseText;
        console.log('queryTeststore result = ' + result);
        return result;
      }

      function dropGraph(uri, scratchStudyUri) {
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('POST', scratchStudyUri + '/update', false);
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

      function executeUpdateQuery(query, scratchStudyUri) {
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('POST', scratchStudyUri + '/update', false);
        xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
        xmlHTTP.send(query);
        return xmlHTTP.responseText;
      }

      return {
        queryTeststore: queryTeststore,
        dropGraph: dropGraph,
        loadTemplate: loadTemplate,
        executeUpdateQuery: executeUpdateQuery
      };
    };

    return TestUtilService;
  }();
