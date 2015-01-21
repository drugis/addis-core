'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the remote rdf service', function() {
    var remoteRdfStoreService;
    beforeEach(module('trialverse'));
    beforeEach(module('trialverse.util'));
    beforeEach(inject(function(RemoteRdfStoreService) {
      remoteRdfStoreService = RemoteRdfStoreService;
    }));
    describe('deFusekify', function() {
      it('should remove the data.results.bindings stack and .value properties', function() {
        var input = {
          'data': {
            'results': {
              'bindings': [{
                'uri': {
                  'type': 'uri',
                  'value': 'http://trials.drugis.org/instances/b28cc394-a142-4f60-af36-8ee9b0783ed7'
                },
                'label': {
                  'type': 'literal',
                  'value': 'adsf'
                },
                'duration': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#duration',
                  'type': 'typed-literal',
                  'value': 'PT1H'
                },
                'isPrimary': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#boolean',
                  'type': 'typed-literal',
                  'value': 'false'
                },
                'pos': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#integer',
                  'type': 'typed-literal',
                  'value': '0'
                }
              }, {
                'uri': {
                  'type': 'uri',
                  'value': 'http://trials.drugis.org/instances/45b68809-92f8-44a2-baec-d6c0bb0cab5d'
                },
                'label': {
                  'type': 'literal',
                  'value': 'asdf'
                },
                'comment': {
                  'type': 'literal',
                  'value': 'safd'
                },
                'duration': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#duration',
                  'type': 'typed-literal',
                  'value': 'PT0S'
                },
                'isPrimary': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#boolean',
                  'type': 'typed-literal',
                  'value': 'true'
                },
                'pos': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#integer',
                  'type': 'typed-literal',
                  'value': '1'
                }
              }]
            }
          }
        };
        var expectedOutput = [{
          uri: 'http://trials.drugis.org/instances/b28cc394-a142-4f60-af36-8ee9b0783ed7',
          label: 'adsf',
          duration: 'PT1H',
          isPrimary: 'false',
          pos: '0'
        }, {
          uri: 'http://trials.drugis.org/instances/45b68809-92f8-44a2-baec-d6c0bb0cab5d',
          label: 'asdf',
          comment: 'safd',
          duration: 'PT0S',
          isPrimary: 'true',
          pos: '1'
        }];
        expect(remoteRdfStoreService.deFusekify(input)).toEqual(expectedOutput);
      });
    });
  });
});
