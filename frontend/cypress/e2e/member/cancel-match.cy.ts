describe('Annuler une réservation', () => {
  it('un membre annule sa réservation', () => {

    cy.visit('http://localhost:4200');

    cy.get('[data-cy="header-login-button"]').click();

    cy.get('[data-cy="login-input"]').type('S0005');

    cy.get('[data-cy="login-button"]').click();

    cy.get('[routerlink="/mes-reservations"]').click();

    cy.get('button')
      .contains(/annuler/i)
      .should('be.visible')
      .click();

    cy.get('button')
      .contains(/oui/i)
      .should('be.visible')
      .click();
  });
});
