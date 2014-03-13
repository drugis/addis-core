define(['angular', 'angular-mocks', 'controllers'],
  function () {
    describe("The analysisController", function () {
      var scope, analysisService;
      var mockAnalysis = {name: 'testName', type="Single-study Benefit-Risk", study = null};

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function ($controller) {
        analysisService = jasmine.createSpyObj('analysisService', ['get']);
        scope = {};

        analysisService.get(1).andReturn(mockAnalysis)

        ctrl = $controller('AnalysisController', {$scope: scope, 'AnalysisService': analysisService});
      }));

      it("should   ")

    });
  }
);
