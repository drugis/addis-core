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

        mockAnalysis = {};
        mockProject = {
          owner: {
            id: 1
          }
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

        it('should place analysis and project on the scope', function() {
          expect(scope.analysis).toBe(mockAnalysis);
          expect(scope.project).toBe(mockProject);
        });

        it('should set isProblemDefined to be false when the controller is initialized ', function() {
          expect(scope.isProblemDefined).toBeFalsy();
        });
      });

      describe('when a non-base analysis state is loaded', function() {
        beforeEach(inject(function($controller) {
          state = jasmine.createSpyObj('$state', ['go']);
          state.current = {
            name: 'not base analysis'
          };

          controllerArguments.$state = state;
          $controller('AnalysisController', controllerArguments);
        }));

        it('should not navigate', function() {
          expect(state.go).not.toHaveBeenCalled();
        });
      });

      describe('when the base analysis is loaded', function() {
        beforeEach(inject(function($controller) {
          state = jasmine.createSpyObj('$state', ['go']);
          state.current = {
            name: 'analysis'
          };

          mockAnalysis.analysisType = ANALYSIS_TYPES[0].label;
          mockAnalysis.id = 1;

          controllerArguments.$state = state;
          $controller('AnalysisController', controllerArguments);
        }));

        it('should navigate to the appropriate state', function() {
          expect(state.go).toHaveBeenCalledWith(ANALYSIS_TYPES[0].stateName, {
            type: mockAnalysis.analysisType,
            analysisId: mockAnalysis.id
          });
        });

      });

      describe('when the analysis is owned by someone else', function() {
        beforeEach(inject(function($controller) {
          mockProject.owner = {
            id: 2
          };
          $controller('AnalysisController', controllerArguments);
        }));

        it('it should not be editable', function() {
          expect(scope.editMode.disableEditing).toBeTruthy();
        });
      });

      describe('when the analysis is owned and no problem is defined', function() {
        beforeEach(inject(function($controller) {
          state = jasmine.createSpyObj('$state', ['go']);

          mockAnalysis.analysisType = ANALYSIS_TYPES[0].label;
          mockAnalysis.id = 1;

          controllerArguments.$state = state;
          $controller('AnalysisController', controllerArguments);
        }));

        it('should allow editing owned analyses', function() {
          expect(scope.project.owner.id).toEqual(mockWindow.config.user.id);
          expect(scope.editMode.disableEditing).toBeFalsy();
        });

        it('isProblemDefined should be false', function() {
          expect(scope.isProblemDefined).toBeFalsy();
        });
      });

      describe('when the analysis has a defined problem', function() {

        beforeEach(inject(function($controller) {
          mockAnalysis.problem = {
            foo: 'bar'
          };
          $controller('AnalysisController', controllerArguments);
        }));

        it('should not be editable', function() {
          expect(scope.editMode.disableEditing).toBeTruthy();
        });

        it('isProblemDefined should be true', function() {
            expect(scope.isProblemDefined).toBeTruthy();
          });
      });

    });
  });