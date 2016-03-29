function DatasetsPage(browser) {
  this.browser = browser;
}

var CLICK_RESULT_STATUS_FAULURE = -1;


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
      .useXpath() // every selector now must be xpath
    .waitForElementVisible(LOCATORS.xPathFeaturedDatasetCreateBtn, 15000)
      .click(LOCATORS.xPathFeaturedDatasetCreateBtn, function(res) {
        console.log('create project btn clicked ');
        if (res && CLICK_RESULT_STATUS_FAULURE === res.status) {
          console.error(res);
        }
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
