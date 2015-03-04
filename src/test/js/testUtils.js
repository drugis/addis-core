// An alternative example could be..
define('testUtils', ['lodash'],
    function(_) {
        var testFusekiUri = 'http://localhost:9876/scratch';
        return {
            queryTeststore: function(query) {
                //console.log('queryTeststore query = ' + query);
                var xmlHTTP = new XMLHttpRequest();
                xmlHTTP.open('POST', testFusekiUri + '/query?output=json', false);
                xmlHTTP.setRequestHeader('Content-type', 'application/sparql-query');
                xmlHTTP.setRequestHeader('Accept', 'application/ld+json');
                xmlHTTP.send(query);
                var result = xmlHTTP.responseText;
                // console.log('queryTeststore result = ' + result);
                return result;
            },
            dropGraph: function(uri) {
                var xmlHTTP = new XMLHttpRequest();
                xmlHTTP.open('POST', testFusekiUri + '/update', false);
                xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
                xmlHTTP.send('DROP GRAPH <' + uri + '>');
                return true;
            },
            loadTemplate: function(templateName, httpBackend) {
                var xmlHTTP = new XMLHttpRequest();
                xmlHTTP.open('GET', 'base/app/sparql/' + templateName, false);
                xmlHTTP.send(null);
                var template = xmlHTTP.responseText;
                httpBackend.expectGET('app/sparql/' + templateName).respond(template);
                return template;
            },
            executeUpdateQuery: function(query) {
                console.log('executeUpdateQuery: ' + query);
                var xmlHTTP = new XMLHttpRequest();
                xmlHTTP.open('POST', testFusekiUri + '/update', false);
                xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
                xmlHTTP.send(query);
                return xmlHTTP.responseText;
            },
            deFusekify: function(data) {
                // console.log('deFusekify on : ' + data);
                var json = JSON.parse(data);
                var bindings = json.results.bindings;
                return _.map(bindings, function(binding) {
                    return _.object(_.map(_.pairs(binding), function(obj) {
                        return [obj[0], obj[1].value];
                    }));
                });
            }

        }
    }
);
