'use strict';
define(['angular-mocks', './unit'], function(angularMocks) {
  describe('the unit service service', function() {

    var rootScope, q,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph']),
      saveDefer,
      unitService;


    beforeEach(function() {
      angular.mock.module('trialverse.unit', function($provide) {
        $provide.value('StudyService', studyServiceMock);
      });
    });

    beforeEach(inject(function($q, $rootScope, UnitService) {
      q = $q;
      rootScope = $rootScope;

      unitService = UnitService;
      saveDefer = q.defer();
      studyServiceMock.saveJsonGraph.and.returnValue(saveDefer.promise);
      saveDefer.resolve();
    }));


    describe('query units', function() {

      beforeEach(function() {
        var graphDefer = q.defer();
        studyServiceMock.getJsonGraph.and.returnValue(graphDefer.promise);
        graphDefer.resolve([{
          '@id': 'http://trials.drugis.org/instances/unitUuid1',
          '@type': 'ontology:Unit',
          'conversionMultiplier': '1.000000e-03',
          'label': 'milligram'
        }, {
          '@id': 'http://trials.drugis.org/instances/unitUuid2',
          '@type': 'ontology:Unit',
          'conversionMultiplier': '1.000000e-00',
          'label': 'liter'
        }]);
      });

      it('should return the units contained in the graph', function(done) {

        // call function under test
        unitService.queryItems().then(function(result) {
          var units = result;

          // verify query result
          expect(units.length).toBe(2);
          expect(units[0].label).toEqual('milligram');
          expect(units[1].label).toEqual('liter');

          done();
        });
        rootScope.$digest();
      });
    });

    describe('merge', function() {
      var sourceUnit = {
        uri: 'http://trials.drugis.org/instances/unitUuid1'
      };
      var targetUnit = {
        uri: 'http://trials.drugis.org/instances/unitUuid2'
      };

      beforeEach(function() {
        var graphDefer = q.defer();
        studyServiceMock.getJsonGraph.and.returnValue(graphDefer.promise);
        graphDefer.resolve([{
          '@type': 'ontology:Study',
          has_activity: [{
            has_drug_treatment: [{
              treatment_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDose1',
                'unit': targetUnit.uri
              }]
            }, {
              treatment_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDose2',
                'unit': 'http://trials.drugis.org/instances/someOtherUnit'
              }]
            }, {
              treatment_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDose3',
                'unit': sourceUnit.uri
              }]
            }]
          }, {
            has_drug_treatment: [{
              treatment_min_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDoseTitrated1',
                'unit': sourceUnit.uri
              }],
              treatment_max_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDoseTitrated2',
                'unit': sourceUnit.uri
              }]

            }]
          }]
        }, {
          '@id': sourceUnit.uri,
          '@type': 'ontology:Unit',
          'conversionMultiplier': '1.000000e-03',
          'label': 'sourceMilligram'
        }, {
          '@id': targetUnit.uri,
          '@type': 'ontology:Unit',
          'conversionMultiplier': '1.000000e-00',
          'label': 'targetMilligram'
        }]);
      });
      it('should remove the source unit and replace the unit of all doses with the source unit with the target unit', function(done) {
        var expectedGraph = [{
          '@type': 'ontology:Study',
          has_activity: [{
            has_drug_treatment: [{
              treatment_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDose1',
                'unit': targetUnit.uri
              }]
            }, {
              treatment_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDose2',
                'unit': 'http://trials.drugis.org/instances/someOtherUnit'
              }]
            }, {
              treatment_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDose3',
                'unit': targetUnit.uri
              }]
            }]
          }, {
            has_drug_treatment: [{
              treatment_min_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDoseTitrated1',
                'unit': targetUnit.uri
              }],
              treatment_max_dose: [{
                '@id': 'http://trials.drugis.org/instances/someDoseTitrated2',
                'unit': targetUnit.uri
              }]
            }]
          }]
        }, {
          '@id': targetUnit.uri,
          '@type': 'ontology:Unit',
          'conversionMultiplier': '1.000000e-00',
          'label': 'targetMilligram'
        }];
        unitService.merge(sourceUnit, targetUnit).then(function() {
          expect(studyServiceMock.saveJsonGraph).toHaveBeenCalledWith(expectedGraph);
          done();
        });
        rootScope.$digest();
      });
    });
  });
});
