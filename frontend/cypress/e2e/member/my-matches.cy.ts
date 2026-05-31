describe('Consultation des réservations', () => {
  it('affiche les réservations du membre connecté', () => {

    cy.visit('http://localhost:4200');

    cy.get('[data-cy="header-login-button"]').click();

    cy.get('[data-cy="login-input"]').type('G0001');

    cy.get('[data-cy="login-button"]').click();

    cy.get('[routerlink="/mes-reservations"]').click();

    cy.contains('G0001').should('be.visible');
  });
});
