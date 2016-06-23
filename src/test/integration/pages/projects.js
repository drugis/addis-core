function ProjectsPage(browser) {
  this.browser = browser;
}

var CLICK_RESULT_STATUS_FAULURE = -1;

var LOCATORS = {
  body: 'body',
  firstProject: '.content.ng-scope.active #project-list a'
};

ProjectsPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
  },
  end: function() {
    this.browser.end();
  },
  selectFirstProject: function() {
    this.browser
      .waitForElementVisible(LOCATORS.firstProject, 15000)
      .click(LOCATORS.firstProject)
      .pause(300);
  },
  selectProjectByName: function(name) {
    this.browser
      .pause(300)
      .useXpath()
      .click('//*[contains(@class, "content ng-scope active")]//a[contains(., "' + name + '")]')
      .useCss()
      .pause(300);
  }
};

module.exports = ProjectsPage;
