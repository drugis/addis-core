'use strict';
define(['angular-mocks'], function () {
  describe('DatasetService', function() {
    var datasetService;
    beforeEach(angular.mock.module('trialverse.dataset'));
    
    beforeEach(inject(function(DatasetService){
      datasetService = DatasetService;
    }));

    describe('filterStudies', function() {
      it('should reject studies that do not include all the selecteddrugs and variables', function() {
        var study1 = {
          drugUris: 'drugUri1, drugUri2, drugUri3',
          outcomeUris: 'variableUri1, variableUri2, variableUri3'
        };
        var study2 = {
          drugUris: 'drugUri1, drugUri2',
          outcomeUris: 'variableUri2'
        };
        var study3 = {
          drugUris: 'drugUri2',
          outcomeUris: 'variableUri1, variableUri2'
        };
        var study4 = {};
        var studies = [study1, study2, study3, study4];
        var filterSelections = {
          drugs: [{
            '@id': 'drugUri1'
          }, {
            '@id': 'drugUri2'
          }],
          variables: [{
            '@id': 'variableUri1'
          }, {
            '@id': 'variableUri2'
          }]
        };
        var result = datasetService.filterStudies(studies, filterSelections);
        var expectedResult = [study1];
        expect(result).toEqual(expectedResult);
      });
    }); 
  });
});