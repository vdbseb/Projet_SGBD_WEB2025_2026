describe('Admin guard', () => {

  it('should allow admin access after login', () => {

    // Tentative accès admin
    cy.visit('http://localhost:4200/admin');

    // Dialog admin affiché
    cy.contains(/connexion administrateur/i)
      .should('be.visible');

    // Login admin
    cy.get('[data-cy="login-input"]')
      .type('AG01');

    cy.get('[data-cy="login-button"]')
      .click();

    // Cliquer sur Administration dans le header
    cy.contains(/administration/i)
      .click();

    // Dashboard admin visible
    cy.contains(/super admin/i)
      .should('be.visible');

  });

});
