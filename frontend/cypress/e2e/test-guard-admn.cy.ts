describe('Admin guard', () => {

  it('should allow admin access after login', () => {

    cy.intercept('POST', '**/api/auth/admin/login').as('adminLogin');

    cy.visit('http://localhost:4200/admin');

    cy.contains(/connexion administrateur/i)
      .should('be.visible');

    cy.get('[data-cy="login-input"]')
      .should('be.visible')
      .clear()
      .type('AG01');

    cy.get('[data-cy="admin-password-input"]')
      .should('be.visible')
      .clear()
      .type('admin123');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.wait('@adminLogin')
      .its('response.statusCode')
      .should('eq', 200);

    cy.window()
      .its('localStorage.padel_admin_token')
      .should('exist');

    cy.visit('http://localhost:4200/admin');

    cy.url({ timeout: 10000 })
      .should('include', '/admin');

    cy.contains(/super admin|admin global|dashboard administrateur|administration/i, { timeout: 10000 })
      .should('be.visible');

  });

});
