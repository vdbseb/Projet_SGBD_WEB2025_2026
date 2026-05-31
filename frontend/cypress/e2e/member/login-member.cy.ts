describe('Connexion membre', () => {
  it('connecte un membre avec le matricule G0001', () => {
    cy.visit('http://localhost:4200');

    cy.get('[data-cy="header-login-button"]').click();

    cy.get('[data-cy="login-input"]').type('G0001');

    cy.get('[data-cy="login-button"]').click();

    cy.get('[routerlink="/mon-espace"]').should('be.visible');
  });
});
