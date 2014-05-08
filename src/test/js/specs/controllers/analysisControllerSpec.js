define(['angular', 'angular-mocks', 'controllers'],
  function() {
    var scope,
      analysisDeferred,
      mockAnalysis,
      projectDeferred,
      mockProject,
      mockWindow = {
        config: {
          user: {
            id: 1
          }
        }
      },
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
          owner: {
            id: 1
          },
          $promise: projectDeferred.promise
        };
        controllerArguments = {
          $scope: scope,
          $state: {},
          $q: $q,
          $window: mockWindow,
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

        it('should set isProblemDefined to be false when the controller is initialized ', function() {
          expect(scope.isProblemDefined).toBeFalsy();
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

        it('should only make loading.loaded true when both project and analysis are loaded', function() {
          expect(scope.loading.loaded).toBeFalsy();
          projectDeferred.resolve();
          scope.$apply();
          expect(scope.loading.loaded).toBeTruthy();
        });

        it('should allow editing owned analyses', function() {
          projectDeferred.resolve();
          scope.$apply();

          expect(scope.project.owner.id).toEqual(mockWindow.config.user.id);
          expect(scope.editMode.disableEditing).toBeFalsy();
        });

        it('should not allow editing of non-owned analyses', function() {
          mockProject.owner = {
            id: 2
          };
          projectDeferred.resolve(mockProject);
          scope.$apply();

          expect(scope.editMode.disableEditing).toBeTruthy();
        });

        it('should not allow editing of an analysis with a defined problem', function() {
          mockAnalysis.problem = {
            foo: 'bar'
          };
          analysisDeferred.resolve(mockAnalysis);
          projectDeferred.resolve();
          scope.$apply();

          expect(scope.editMode.disableEditing).toBeTruthy();
        });

        it('should set isProblemDefined to be false when the project and analysis have been loaded, but the analysis has no problem defined',
          function() {
            mockAnalysis.problem = null;
            projectDeferred.resolve();
            analysisDeferred.resolve(mockAnalysis);
            scope.$apply();
            expect(scope.isProblemDefined).toBeFalsy();
          });

        it('should set isProblemDefined to be true when the project and analysis have been loaded, and the analysis has a problem defined',
          function() {
            mockAnalysis.problem = {
              mock: 'problem'
            };
            projectDeferred.resolve();
            analysisDeferred.resolve(mockAnalysis);
            scope.$apply();
            expect(scope.isProblemDefined).toBeTruthy();
          });

      });
    });
  });