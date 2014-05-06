define(['angular', 'angular-mocks', 'controllers'], function() {
  describe('the network meta-analysis controller', function() {
    var scope;

    beforeEach(module('addis.controllers'));

    describe('when first initialised', function() {
      beforeEach(inject(function($rootScope, $controller, $q) {
        var analysisDeferred,
          mockAnalysis = {};

        analysisDeferred = $q.defer();
        mockAnalysis.$promise = analysisDeferred.promise;



        scope = $rootScope;
        scope.$parent = {
          analysis: mockAnalysis
        };

        $controller('NetworkMetaAnalysisController', {
          $scope: scope
        });
      }));

      it('should inherit the parent\'s analysis', function() {
        expect(scope.analysis).toEqual(scope.$parent.analysis);
      });

      it('loading.loaded should be false', function() {
        expect(scope.$parent.loading.loaded).toBeFalsy();
      });
    });
  });
});