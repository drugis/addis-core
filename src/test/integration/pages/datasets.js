function DatasetsPage(browser) {
  this.browser = browser;
}

var LOCATORS = {
  body: 'body',
  featuredDatasetCreateBtn: '.featured-dataset-col button:nth-of-type(1)', // just select the first featured dataset
  xPathFeaturedDatasetCreateBtn: '//button[@type="button" and contains(., "Create project")]',
  projectNameInput: 'form[ng-submit="createProject(project)"] input[ng-model="project.name"]',
  projectDescriptionTxtFld: 'form[ng-submit="createProject(project)"] textarea[ng-model="project.description"]',
  createProjectModelCreateBtn: 'form[ng-submit="createProject(project)"] button[type="submit"]'
};

DatasetsPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
  },
  end: function() {
    this.browser.end();
  },
  createFeaturedDatasetProject: function(name, desc) {
    this.browser
      .pause(3000)
      .useXpath() // every selector now must be xpath
      .click(LOCATORS.xPathFeaturedDatasetCreateBtn, function(res){
        console.log('bt clicked ');
        console.log(res);
      })
      .useCss() // we're back to CSS now
      .pause(3000)
      .clearValue(LOCATORS.projectNameInput)
      .setValue(LOCATORS.projectNameInput, name)
      .clearValue(LOCATORS.projectDescriptionTxtFld)
      .setValue(LOCATORS.projectDescriptionTxtFld, desc)
      .pause(1000)
      .click(LOCATORS.createProjectModelCreateBtn).pause(3000);
  }
};

module.exports = DatasetsPage;
