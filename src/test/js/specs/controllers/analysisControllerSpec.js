define(['angular', 'angular-mocks', 'controllers'],
  function() {
    var scope,
      analysisDeferred,
      mockAnalysis,
      projectDeferred,
      mockProject,
      controllerArguments,
      state,
      ANALYSIS_TYPES = [{
        label: 'test type 1',
        stateName: 'test state 1'
      }, {
        label: 'test type 2',
        stateName: 'test state 2'
      }];
    describe('the analysisController', function() {

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($rootScope, $q) {
        scope = $rootScope;

        analysisDeferred = $q.defer();
        mockAnalysis = {
          $promise: analysisDeferred.promise
        };
        projectDeferred = $q.defer();
        mockProject = {
          $promise: projectDeferred.promise
        };
        controllerArguments = {
          $scope: scope,
          $state: {},
          $q: $q,
          currentAnalysis: mockAnalysis,
          currentProject: mockProject,
          ANALYSIS_TYPES: ANALYSIS_TYPES
        };
      }));

      describe('when first initialised', function() {
        beforeEach(inject(function($controller, $q) {
          $controller('AnalysisController', controllerArguments);
        }));

        it('should set loading.loaded to false', function() {
          expect(scope.loading.loaded).toBeFalsy();
        });

        it('should place analysis and project on the scope', function() {
          expect(scope.analysis).toBe(mockAnalysis);
          expect(scope.project).toBe(mockProject);
        });
      });

      describe('when the analysis is loaded', function() {
        beforeEach(inject(function($controller) {
          state = jasmine.createSpyObj('$state', ['go']);

          mockAnalysis.analysisType = ANALYSIS_TYPES[0].label;
          mockAnalysis.id = 1;

          controllerArguments.$state = state;
          $controller('AnalysisController', controllerArguments);

          analysisDeferred.resolve(mockAnalysis);
          scope.$apply();
        }));

        it('should navigate to the appropriate state', function() {
          expect(state.go).toHaveBeenCalledWith(ANALYSIS_TYPES[0].stateName, {
            type: mockAnalysis.analysisType,
            analysisId: mockAnalysis.id
          });
        });

        it('and the project is loaded should set loading.loaded to true', function() {
          projectDeferred.resolve();
          expect(scope.loading.loaded).toBeTruthy();
        })
      });
    });
  });