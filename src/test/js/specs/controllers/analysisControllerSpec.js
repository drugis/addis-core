define(['angular', 'angular-mocks', 'controllers'],
  function() {
    var scope,
      analysisDeferred,
      mockAnalysis,
      projectDeferred,
      mockProject;
    describe('the analysisController', function() {

      beforeEach(module('addis.controllers'));

      describe('when first initialised', function() {

        beforeEach(inject(function($rootScope, $controller, $q) {
          scope = $rootScope;

          analysisDeferred = $q.defer();
          mockAnalysis = {
            $promise: analysisDeferred.promise
          };
          projectDeferred = $q.defer();
          mockProject = {
            $promise: projectDeferred.promise
          };



          $controller('AnalysisController', {
            $scope: scope,
            $state: {},
            currentAnalysis: mockAnalysis,
            currentProject: mockProject,
            ANALYSIS_TYPES: {}
          })
        }));

        it('should set loading.loaded to false', function() {
          expect(scope.loading.loaded).toBeFalsy();
        });

      });

    });


  });